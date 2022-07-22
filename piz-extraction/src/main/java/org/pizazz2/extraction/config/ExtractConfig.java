package org.pizazz2.extraction.config;

import org.pizazz2.PizContext;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

/**
 * 提取组件全局配置
 *
 * @author xlgp2171
 * @version 2.1.220722
 */
public class ExtractConfig {

    private Path baseDirectory;
    /** BaseType的白名单 */
    private final Set<String> typeWhiteList = new HashSet<>();
    /** BaseType的黑名单 */
    private final Set<String> typeBlackList = new HashSet<>();

    public ExtractConfig() {
        baseDirectory = PizContext.TEMP_DIRECTORY.resolve(PizContext.NAMING_SHORT + "_extraction_temp");
    }

    public ExtractConfig setTypeWhiteList(Set<String> typeWhiteList) {
        this.typeWhiteList.addAll(typeWhiteList);
        return this;
    }

    public Set<String> getTypeWhiteList() {
        return typeWhiteList;
    }

    public ExtractConfig setTypeBlackList(Set<String> typeBlackList) {
        this.typeBlackList.addAll(typeBlackList);
        return this;
    }

    public Set<String> getTypeBlackList() {
        return typeBlackList;
    }

    public void setBaseDirectory(Path baseDirectory) {
        this.baseDirectory = baseDirectory;
    }

    public Path getBaseDirectory() {
        return baseDirectory;
    }
}
