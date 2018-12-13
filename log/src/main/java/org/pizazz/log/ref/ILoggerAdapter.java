package org.pizazz.log.ref;

import java.nio.file.Path;

import org.pizazz.log.Logger;
import org.pizazz.log.record.RecordRunnable;
import org.pizazz.IPlugin;


/**
 * 日志输出器适配接口
 *
 * @author xlgp2171
 * @version 1.0.181210
 */
public interface ILoggerAdapter extends IPlugin {

    /**
     * 获取日志输出器
     * @param key 分类键
     * @param runnable
     * @return 日志输出器, 后验条件: 不返回null.
     */
    public Logger getLogger(Class<?> key, RecordRunnable runnable);

    /**
     * 获取日志输出器
     * @param key 分类键
     * @param runnable
     * @return 日志输出器, 后验条件: 不返回null.
     */
    public Logger getLogger(String key, RecordRunnable runnable);

    /**
     * 设置输出等级
     * @param level 输出等级
     */
    public void setLevel(LogEnum level);

    /**
     * 获取当前日志等级
     * @return 当前日志等级
     */
    public LogEnum getLevel();

    /**
     * 获取当前日志文件
     * @return 当前日志文件
     */
    public Path getPath();
}