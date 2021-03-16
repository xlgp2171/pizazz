package org.pizazz2.tool;

import org.pizazz2.ICloseable;
import org.pizazz2.common.ArrayUtils;
import org.pizazz2.common.PathUtils;
import org.pizazz2.common.SystemUtils;
import org.pizazz2.common.ValidateUtils;
import org.pizazz2.exception.ToolException;
import org.pizazz2.exception.UtilityException;
import org.pizazz2.helper.LocaleHelper;
import org.pizazz2.message.BasicCodeEnum;
import org.pizazz2.message.TypeEnum;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * 共享内存映射传输工具
 *
 * @author xlgp2171
 * @version 2.0.210201
 */
public class MemoryMapping implements ICloseable {

    private final FileChannel channel;
    private final MappedByteBuffer buffer;

    public MemoryMapping(long size) throws UtilityException, ToolException {
        this(PathUtils.copyToTemp(ArrayUtils.EMPTY_BYTE, "piz_mm_"), 0, size);
    }

    /**
     *
     * @param path 映射文件路径，
     * @param position 文件映射起始位置
     * @param size 文件映射长度
     * @throws ToolException 映射异常
     */
    public MemoryMapping(Path path, long position, long size) throws ToolException {
        ValidateUtils.notNull("MemoryMapping", path);
        try {
            channel = FileChannel.open(path, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.READ);
            buffer = channel.map(FileChannel.MapMode.READ_WRITE, position, size);
        } catch (IOException e) {
            String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.MAPPING.NEW", path.toAbsolutePath(), e.getMessage());
            throw new ToolException(BasicCodeEnum.MSG_0003, msg, e);
        }
    }

    /**
     * 发送数据
     * @param func 包含发送数据
     * @param sessionId 唯一标识ID，每次发送接收必须保持一致
     * @return 是否发送成功
     * @throws ToolException 文件映射异常
     */
    public boolean sentMessage(Function<FileLock, byte[]> func, long sessionId) throws ToolException {
        if (func != null) {
            try (FileLock lock = channel.tryLock()) {
                byte[] data = func.apply(lock);

                if (lock != null && data != null) {
                    // 写入之前清除数据
                    buffer.clear();
                    buffer.putLong(sessionId).putInt(data.length).put(data);
                    // xlgp: 是否应该重新加载?
                    buffer.load();
                    return true;
                }
            } catch (IOException e) {
                String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.MAPPING.MSG", e.getMessage());
                throw new ToolException(BasicCodeEnum.MSG_0003, msg, e);
            }
        }
        return false;
    }

    /**
     * 接收数据
     * @param func 用于验证是否同一个sessionId
     * @return 为null则数据未接收成功
     * @throws ToolException 文件映射异常
     */
    public byte[] receiveMessage(Predicate<Long> func) throws ToolException {
        buffer.position(0);

        try (FileLock lock = channel.tryLock()) {
            if (lock == null) {
                return null;
            }
            // 获取会话ID
            long sessionId = buffer.getLong();

            if (func != null && func.test(sessionId)) {
                byte[] data = new byte[buffer.getInt()];

                if (data.length != 0) {
                    buffer.get(data);
                    buffer.clear();
                    return data;
                }
            }
        } catch (IOException e) {
            String msg = LocaleHelper.toLocaleText(TypeEnum.BASIC, "ERR.MAPPING.MSG", e.getMessage());
            throw new ToolException(BasicCodeEnum.MSG_0003, msg, e);
        }
        return null;
    }

    @Override
    public void destroy(Duration timeout) {
        SystemUtils.close(channel);
    }
}
