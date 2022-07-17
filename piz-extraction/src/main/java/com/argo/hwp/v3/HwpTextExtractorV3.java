/*
   Copyright [2015] argonet.co.kr

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
/*
 * This software has been developed with reference to
 * the HWP file format open specification by Hancom, Inc.
 * http://www.hancom.co.kr/userofficedata.userofficedataList.do?menuFlag=3
 * 한글과컴퓨터의 한/글 문서 파일(.hwp) 공개 문서를 참고하여 개발하였습니다.
 *
 * 본 제품은 다음의 소스를 참조하였습니다.
 * https://github.com/cogniti/ruby-hwp/
 */
package com.argo.hwp.v3;

import java.io.*;
import java.util.Arrays;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.argo.hwp.utils.HwpStreamReader;

/**
 * @author argonet.co.kr
 * @version 1.0.update
 */
public final class HwpTextExtractorV3 {
    private static final Logger LOGGER = LoggerFactory.getLogger(HwpTextExtractorV3.class);

    /**
     * 1byte 문자들..
     */
    private static final byte[] HWP_V3_SIGNATURE = ("HWP Document File V3.00" +
			" \u001A\u0001\u0002\u0003\u0004\u0005").getBytes();

    public static String extractText(byte[] data) throws IOException {
		// 若数据字节小于版本标识
		if (data == null || data.length < HWP_V3_SIGNATURE.length) {
			return "";
		}
		// 读取版本标识
		byte[] buf = Arrays.copyOfRange(data, 0, HWP_V3_SIGNATURE.length);
		// 若版本标识无法对应
		if (!Arrays.equals(HWP_V3_SIGNATURE, buf)) {
			return "";
		}
        byte[] tmp = Arrays.copyOfRange(data, HWP_V3_SIGNATURE.length, data.length);

		try (InputStream in = new ByteArrayInputStream(tmp)) {
			StringWriter writer = new StringWriter();
			extractText(in, writer);
			return writer.toString();
		}
	}

    public static boolean extractText(File source, Writer writer) throws IOException {
        InputStream input = new FileInputStream(source);
        try {
            // 한글V3 시그니처 확인
            try {
                byte[] buf = new byte[HWP_V3_SIGNATURE.length];
                int read = input.read(buf);
                if (read < HWP_V3_SIGNATURE.length) {
					return false;
				}
                // 시그니처 확인
                if (!Arrays.equals(HWP_V3_SIGNATURE, buf)) {
					return false;
				}
            } catch (IOException e) {
                LOGGER.warn("文件信息确认中错误。视为非HWP格式", e);
                return false;
            }
            extractText(input, writer);
            return true;
        } finally {
            try {
                // from javadoc. If this file has an associated channel then the
                // channel is closed as well.
                input.close();
            } catch (IOException e) {
                LOGGER.warn("exception while file.close", e);
            }
        }
    }

    private static void extractText(InputStream inputStream, Writer writer) throws IOException {
        // 시그니처를 위해서 30바이트 읽은 상태
        HwpStreamReader input = new HwpStreamReader(inputStream);
        // 문서 정보 p.72
        // 암호 걸린 파일 확인
        input.ensureSkip(96);
        int t = input.uint16();

        if (t != 0) {
			throw new IOException("加密过的文件是无法解析的");
		}
        // 압축 확인
		// 124
        input.ensureSkip(26);
        boolean compressed = input.uint8() != 0;
        // 정보 블럭 길이
        input.ensureSkip(1);
        int blockSize = input.uint16();
        // 문서 요약 건너뛰기
        input.ensureSkip(1008);
        // 정보 블럭 건너뛰기
        input.ensureSkip(blockSize);
        // 압축 풀기
        if (compressed) {
            // log.info("본문 압축 해제");
            input = new HwpStreamReader(new InflaterInputStream(inputStream, new Inflater(true)));
        }
        // p.73 글꼴이름 건너뛰기
        for (int ii = 0; ii < 7; ii++) {
			input.ensureSkip(input.uint16() * 40L);
		}
        // p.74 스타일 건너뛰기
        input.ensureSkip((long) input.uint16() * (20 + 31 + 187));
        // <문단 리스트> ::= <문단>+ <빈문단>
        // int paraCount = 0;
        while (input.available()) {
            // paraCount++;
            // log.debug("문단 {}", paraCount);
            if (!writeParaText(input, writer)) {
				break;
			}
        }
    }

    private static boolean writeParaText(HwpStreamReader input, Writer writer) throws IOException {
        // # 문단 정보
        short prev_paragraph_shape = input.uint8();
        int n_chars = input.uint16();
        int n_lines = input.uint16();
        short char_shape_included = input.uint8();
        StringBuilder buf = new StringBuilder();
        // p.77 기타 플래그부터..
        input.ensureSkip(1 + 4 + 1 + 31);
        // # 여기까지 43 bytes
        if (prev_paragraph_shape == 0 && n_chars > 0) {
			input.ensureSkip(187);
		}
        // # 빈문단이면 false 반환
        if (n_chars == 0) {
            // log.debug("빈문단");
            return false;
        }
        // # 줄 정보
        input.ensureSkip(n_lines * 14L);
        // # 글자 모양 정보 p.78
        if (char_shape_included != 0) {
            for (int ii = 0; ii < n_chars; ii++) {
                short flag = input.uint8();
                if (flag != 1) {
					input.ensureSkip(31);
				}
            }
        }
        // # 글자들
        int n_chars_read = 0;

        while (n_chars_read < n_chars) {
			// # 2바이트씩 읽는다.
            int c = input.uint16();
            n_chars_read++;

            switch (c) {
				// 필드코드(덧말, 계산식, 환경정보, 누름틀)
                case 5:
                {
					// 정보 길이
                    long len = input.uint32();
                    input.uint16(); // 5
                    n_chars_read += 3;
                    input.ensureSkip(len);
                }
                break;
				// 책갈피
                case 6:
                    n_chars_read += 3;
                    input.ensureSkip(6 + 34);
                    break;
				// tab
                case 9:
                    n_chars_read += 3;
                    input.ensureSkip(6);
                    writer.write('\t');
                    break;
				// 표
                case 10:
                    n_chars_read += 3;
                    input.ensureSkip(6);
                    // # 테이블 식별 정보 84 바이트
                    input.ensureSkip(80);
                    int n_cells = input.uint16();
                    input.ensureSkip(2);
                    input.ensureSkip(27L * n_cells);
                    // # <셀 문단 리스트>+
                    for (int ii = 0; ii < n_cells; ii++) {
                        // # <셀 문단 리스트> ::= <셀 문단>+ <빈문단>
                        // log.debug("셀 {}/{}", ii, n_cells);
                        while (writeParaText(input, writer)) {
							// do nothing
						}
                    }
                    // # <캡션 문단 리스트> ::= <캡션 문단>+ <빈문단>
                    while (writeParaText(input, writer)) {
						// do nothing
					}
                    break;
				// 그림
                case 11:
                {
                    n_chars_read += 3;
                    input.ensureSkip(6);
                    long len = input.uint32();
                    input.ensureSkip(344);
                    input.ensureSkip(len);
                    // # <캡션 문단 리스트> ::= <캡션 문단>+ <빈문단>
                    while (writeParaText(input, writer)) {
						// do nothing
					}
                }
                break;
				// # 글자들 끝
                case 13:
                    writer.write('\n');
                    break;
				// # 머리말/꼬리말
                case 16:
                    n_chars_read += 3;
                    input.ensureSkip(6);
                    input.ensureSkip(10);
                    // # <문단 리스트> ::= <문단>+ <빈문단>
                    while (writeParaText(input, writer)) {
						// do nothing
					}
                    break;
				// # 각주/미주
                case 17:
                    n_chars_read += 3;
                    input.ensureSkip(6);
                    // # 각주/미주 정보 건너 뛰기
                    input.ensureSkip(14);
                    while (writeParaText(input, writer)) {
						// do nothing
					}
                    break;
                case 18:
                case 19:
                case 20:
                case 21:
                    n_chars_read += 3;
                    input.ensureSkip(6);
                    break;
				// # 글자 겹침
                case 23:
                    n_chars_read += 4;
                    input.ensureSkip(8);
                    break;
                case 24:
                case 25:
                    n_chars_read += 2;
                    input.ensureSkip(4);
                    break;
				// # 개요 모양/번호
                case 28:
                    n_chars_read += 31;
                    input.ensureSkip(62);
                    break;
                case 30:
                case 31:
                    n_chars_read += 1;
                    input.ensureSkip(2);
                    break;
                default:
					// # hnc code range
                    if (c >= 0x0020 && c <= 0xffff) {
                        String s = Hnc2String.convert(c);

                        if (s == null) {
                            LOGGER.warn("无映射字符 {}", Integer.toHexString(c));
                            writer.write(unknown(c));
                        } else {
                            buf.append(s);
                            writer.write(s);
                        }
                    } else {
                        LOGGER.error("特殊字符 ? : {}", Integer.toHexString(c));
                        // throw new NotImplementedException();
                    }
            }
        }
        return true;
    }

    private static String unknown(int c) {
        return String.format("?+0x%1$04x", c);
    }
}
