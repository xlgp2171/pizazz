package org.pizazz.log.record;

import org.pizazz.IPlugin;
import org.pizazz.data.TupleObject;
import org.pizazz.exception.BaseException;
import org.pizazz.log.exception.LogError;

/**
 * 日志监控接口
 * 
 * @author xlgp2171
 * @version 1.1.190220
 */
public interface ILoggerRecord extends IPlugin {
	@Override
	public void initialize(TupleObject config) throws BaseException;
    public String record(RecordEntity entity) throws LogError;
    public String filter(RecordEntity entity) throws LogError;
}
