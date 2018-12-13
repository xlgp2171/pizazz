package org.pizazz.log.record;

import org.pizazz.IPlugin;
import org.pizazz.log.exception.LogError;

/**
 * 日志监控接口
 * 
 * @author xlgp2171
 * @version 1.0.181210
 */
public interface ILoggerRecord extends IPlugin {
    public String record(RecordEntity entity) throws LogError;
    public String filter(RecordEntity entity) throws LogError;
}
