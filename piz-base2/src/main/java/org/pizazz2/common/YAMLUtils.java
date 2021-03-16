package org.pizazz2.common;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import org.pizazz2.PizContext;
import org.pizazz2.data.TupleObject;
import org.pizazz2.exception.ValidateException;
import org.pizazz2.exception.UtilityException;
import org.pizazz2.helper.LocaleHelper;
import org.pizazz2.helper.TupleObjectHelper;
import org.pizazz2.message.BasicCodeEnum;
import org.pizazz2.message.TypeEnum;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

/**
 * 对象YAML处理<br>
 * 使用snakeyaml组件
 * 
 * @author xlgp2171
 * @version 2.0.210201
 */
public class YAMLUtils {

	public static <T> T fromYAML(InputStream target, Class<T> type) throws ValidateException, UtilityException {
		ValidateUtils.notNull("fromYAML", target, type);
		try {
			return new Yaml().loadAs(target, type);
		} catch(YAMLException e) {
			String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.SNAKEYAML.PROCESS", e.getMessage());
			throw new UtilityException(BasicCodeEnum.MSG_0013, msg, e);
		}
	}

	public static TupleObject fromYAML(InputStream target) throws ValidateException, UtilityException {
		ValidateUtils.notNull("fromYAML", target);
		try {
			return YAMLUtils.fromYAML(target, TupleObject.class);
		} finally {
			SystemUtils.close(target);
		}
	}

	/**
	 * 从资源路径读取YAML文件转换为通用对象
	 * @param resource 资源路径
	 * @return 通用对象
	 * @throws ValidateException 参数验证
	 * @throws UtilityException YAML转换异常
	 */
	public static TupleObject fromYAML(String resource) throws ValidateException, UtilityException {
		try (InputStream tmp = IOUtils.getResourceAsStream(resource, PizContext.class, null)) {
			return YAMLUtils.fromYAML(tmp, TupleObject.class);
		} catch (IOException e) {
			return TupleObjectHelper.emptyObject();
		}
	}

	public static String toYAMLString(Path path, TupleObject data) throws ValidateException, UtilityException {
		ValidateUtils.notNull("fromYAML", path, data);
		try {
			return new Yaml().dumpAsMap(data);
		} catch (YAMLException e) {
			String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.SNAKEYAML.PROCESS", e.getMessage());
			throw new UtilityException(BasicCodeEnum.MSG_0013, msg, e);
		}
	}

	/**
	 * 将通用对象写入YAML文件
	 * @param path YAML文件路径
	 * @param data 数据
	 * @throws ValidateException 参数验证
	 * @throws UtilityException 数据转换异常
	 */
	public static void toYAML(Path path, TupleObject data) throws ValidateException, UtilityException {
		ValidateUtils.notNull("fromYAML", path, data);
		String tmp = YAMLUtils.toYAMLString(path, data);
		PathUtils.copyToPath(tmp.getBytes(SystemUtils.LOCAL_ENCODING), path);
	}
}
