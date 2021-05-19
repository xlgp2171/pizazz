package org.pizazz2.extraction.config;

import org.apache.tika.mime.MediaType;
import org.pizazz2.PizContext;
import org.pizazz2.common.PathUtils;
import org.pizazz2.exception.UtilityException;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

/**
 * 提取组件全局配置
 *
 * @author xlgp2
 * @version 2.0.210501
 */
public class ExtractConfig {

    private Path baseDirectory;
    /**
     * BaseType的白名单
     */
    private final Set<String> typeWhiteList = new HashSet<>();

    public ExtractConfig() {
        baseDirectory = PizContext.TEMP_DIRECTORY.resolve(PizContext.NAMING_SHORT + "_extraction_temp");
    }

    public Set<String> getTypeWhiteList() {
        return typeWhiteList;
    }

    public void setBaseDirectory(Path baseDirectory) {
        this.baseDirectory = baseDirectory;
    }

    public Path getBaseDirectory() {
        return baseDirectory;
    }
}
