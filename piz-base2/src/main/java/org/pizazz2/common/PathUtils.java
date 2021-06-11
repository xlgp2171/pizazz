package org.pizazz2.common;

import org.pizazz2.PizContext;
import org.pizazz2.exception.UtilityException;
import org.pizazz2.exception.ValidateException;
import org.pizazz2.helper.LocaleHelper;
import org.pizazz2.message.BasicCodeEnum;
import org.pizazz2.message.TypeEnum;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 文件工具
 *
 * @author xlgp2171
 * @version 2.0.210610
 */
public class PathUtils {

    public static URI toURI(String uri) throws ValidateException, UtilityException {
        ValidateUtils.notNull("toURI", uri);
        try {
            return new URI(uri);
        } catch (URISyntaxException e1) {
            try {
                return new URI(uri.replaceAll(StringUtils.SPACE, "%20"));
            } catch (URISyntaxException e2) {
                try {
                    return new URI("string", null, uri, null);
                } catch (URISyntaxException e3) {
                    String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.PATH.FORMAT", "URI", uri, e3.getMessage());
                    throw new UtilityException(BasicCodeEnum.MSG_0005, msg, e3);
                }
            }
        }
    }

    public static URI resolve(URI uri, String target) {
        URI tmp;
        try {
            tmp = PathUtils.toURI(StringUtils.of(uri) + (StringUtils.of(uri).endsWith("/") ? StringUtils.EMPTY : "/"));
        } catch (UtilityException e) {
            return uri;
        }
        String newTarget;
        try {
            newTarget = URLEncoder.encode(target, PizContext.LOCAL_ENCODING.name());
        } catch (UnsupportedEncodingException e) {
            return uri;
        }
        return tmp.resolve(newTarget.replaceAll("\\+", "%20")
				.replaceAll("\\%21", "!").replaceAll("\\%27", "'")
				.replaceAll("\\%28", "(").replaceAll("\\%29", ")")
				.replaceAll("\\%7E", "~"));
    }

    public static boolean exists(Path path) {
        return path != null && Files.exists(path);
    }

    public static boolean directoryExists(Path path) {
        return PathUtils.exists(path) && Files.isDirectory(path);
    }

    public static boolean isRegularFile(Path path) {
        return PathUtils.exists(path) && Files.isReadable(path) && Files.isRegularFile(path);
    }

    public static boolean isDirectory(Path path) {
        return Files.isDirectory(path);
    }

    public static byte[] toByteArray(Path path) throws ValidateException, UtilityException {
        ValidateUtils.notNull("toByteArray", path);
        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {
            String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.IO.PATH", path.toAbsolutePath(), e.getMessage());
            throw new UtilityException(BasicCodeEnum.MSG_0003, msg, e);
        }
    }

    public static void delete(Path path, boolean deep) throws UtilityException {
        if (path != null) {
            if (PathUtils.isDirectory(path) && deep) {
                PathUtils.deleteDirectory(path);
            }
            try {
                Files.delete(path);
            } catch (IOException e) {
                // do nothing
            }
        }
    }

    static void deleteDirectory(Path dir) throws UtilityException {
        try (Stream<Path> tmp = Files.list(dir)) {
            tmp.forEach(item -> {
                try {
                    PathUtils.delete(item, true);
                } catch (UtilityException e) {
                    // do nothing
                }
            });
        } catch (IOException e) {
            String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.IO.PATH", dir.toAbsolutePath(), e.getMessage());
            throw new UtilityException(BasicCodeEnum.MSG_0003, msg, e);
        }
    }

    static Path[] paths(Stream<Path> stream, Predicate<Path> filter, boolean includeDir) {
        Stream<Path> tmp = stream;

        if (includeDir) {
            if (filter != null) {
                tmp = tmp.filter(filter);
            }
        } else {
            if (filter != null) {
                tmp = tmp.filter(item -> Files.isRegularFile(item) && Files.isReadable(item) && filter.test(item));
            } else {
                tmp = tmp.filter(item -> Files.isRegularFile(item) && Files.isReadable(item));
            }
        }
        return tmp.toArray(Path[]::new);
    }

    /**
     * 遍历文件夹下所有文件和文件夹
     * @param dir 目标文件夹
     * @param filter 过滤器
     * @param includeDir 输出路径是否包含文件夹
     * @return 所有的路径
     * @throws UtilityException 遍历异常
     * @throws ValidateException 参数验证异常
     */
    public static Path[] listPaths(Path dir, Predicate<Path> filter, boolean includeDir) throws UtilityException,
            ValidateException {
        ValidateUtils.notNull("listPaths", dir);

        try (Stream<Path> stream = Files.list(dir)) {
            return PathUtils.paths(stream, filter, includeDir);
        } catch (IOException e) {
            String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.IO.PATH", dir.toAbsolutePath(), e.getMessage());
            throw new UtilityException(BasicCodeEnum.MSG_0003, msg, e);
        }
    }

    /**
     * 深层遍历文件夹下所有文件和文件夹
     * @param dir 目标文件夹
     * @param filter 过滤器
     * @param includeDir 输出路径是否包含文件夹
     * @return 所有的路径
     * @throws UtilityException 遍历异常
     * @throws ValidateException 参数验证异常
     */
    public static Path[] walkPaths(Path dir, Predicate<Path> filter, boolean includeDir) throws UtilityException,
			ValidateException {
        ValidateUtils.notNull("walkPaths", dir);

        try (Stream<Path> stream = Files.walk(dir)) {
            return PathUtils.paths(stream, filter, includeDir);
        } catch (IOException e) {
            String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.IO.PATH", dir.toAbsolutePath(), e.getMessage());
            throw new UtilityException(BasicCodeEnum.MSG_0003, msg, e);
        }
    }

    public static List<String> readLine(Path path, Charset charset) throws UtilityException {
        ValidateUtils.notNull("readLine", path, charset);
        try {
            return Files.lines(path, charset).collect(Collectors.toList());
        } catch (IOException e) {
            String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.IO.PATH", path.toAbsolutePath(), e.getMessage());
            throw new UtilityException(BasicCodeEnum.MSG_0003, msg, e);
        }
    }

    public static long copyToPath(byte[] data, Path path) throws ValidateException, UtilityException {
        ValidateUtils.notNull("copyToPath", data, path);
        return PathUtils.copyToPath(new ByteArrayInputStream(data), path);
    }

    public static long copyToPath(InputStream in, Path path) throws ValidateException, UtilityException {
        ValidateUtils.notNull("copyToPath", in, path);
        try {
            return Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.PATH", path.toAbsolutePath(), e.getMessage());
            throw new UtilityException(BasicCodeEnum.MSG_0003, msg, e);
        }
    }

    public static Path copy(Path source, Path target) throws ValidateException, UtilityException {
        ValidateUtils.notNull("copy", source, target);
        try {
            return Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.PATH.COPY", source.toAbsolutePath(),
					target.toAbsolutePath(), e.getMessage());
            throw new UtilityException(BasicCodeEnum.MSG_0003, msg, e);
        }
    }

    public static Path createDirectories(Path dir) throws ValidateException, UtilityException {
        ValidateUtils.notNull("createDirectories", dir);
        try {
            return Files.createDirectories(dir);
        } catch (IOException e) {
            String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.PATH.DIR", dir.toAbsolutePath(),
					e.getMessage());
            throw new UtilityException(BasicCodeEnum.MSG_0003, msg, e);
        }
    }

    public static Path createTempDirectory(String prefix) throws ValidateException, UtilityException {
        try {
            return Files.createTempDirectory(prefix);
        } catch (IOException e) {
            String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.PATH.DIR.TEMP", e.getMessage());
            throw new UtilityException(BasicCodeEnum.MSG_0003, msg, e);
        }
    }

    public static Path copyToTemp(Path path, String prefix) throws ValidateException, UtilityException {
        return PathUtils.copyToTemp(PathUtils.toByteArray(path), prefix);
    }

    public static Path copyToTemp(byte[] data, String prefix) throws UtilityException {
        if (data == null) {
            data = new byte[0];
        }
        Path tmp;
        try {
            tmp = Files.createTempFile(prefix, ".tmp");
        } catch (IOException e) {
            String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.PATH.TEMP", e.getMessage());
            throw new UtilityException(BasicCodeEnum.MSG_0003, msg, e);
        }
        if (!ArrayUtils.isEmpty(data)) {
            try (ByteArrayInputStream in = new ByteArrayInputStream(data)) {
                Files.copy(in, tmp, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.PATH.WRITE", tmp.toAbsolutePath(), e.getMessage());
                throw new UtilityException(BasicCodeEnum.MSG_0003, msg, e);
            }
        }
        return tmp;
    }
}
