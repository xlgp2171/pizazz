package org.pizazz2.extraction.process;

import org.apache.tika.metadata.Metadata;
import org.pizazz2.PizContext;
import org.pizazz2.common.JSONUtils;
import org.pizazz2.common.PathUtils;
import org.pizazz2.common.StringUtils;
import org.pizazz2.common.ValidateUtils;
import org.pizazz2.data.TupleObject;
import org.pizazz2.exception.UtilityException;
import org.pizazz2.exception.ValidateException;
import org.pizazz2.extraction.data.ExtractObject;
import org.pizazz2.helper.TupleObjectHelper;

import java.nio.file.Path;

/**
 * 导出处理器
 *
 * @author xlgp2171
 * @version 2.0.210512
 */
public class ExportProcessor {

    private Path baseDirectory;
    private boolean includeMetadata = false;

    public ExportProcessor() throws UtilityException {
        this(PizContext.LOCAL_PATH);
    }

    public ExportProcessor(Path baseDirectory) throws UtilityException {
        setBaseDirectory(baseDirectory);
    }

    protected void export(ExtractObject object, Path baseDirectory) throws UtilityException {
        String name = toPathName(object);
        // 未被处理过
        if (object.getStatus() == ExtractObject.StatusEnum.READY) {
            if (object.getData() != null) {
                PathUtils.copyToPath(object.getData(), baseDirectory.resolve(name));
            }
        } else if (!StringUtils.isEmpty(object.getContent())) {
            PathUtils.copyToPath(object.getContent().getBytes(), baseDirectory.resolve(name + ".txt"));
        }
        if (includeMetadata) {
            export(object.getMetadata(), baseDirectory.resolve(name + ".metadata"));
        }
    }

    public void exportAll(ExtractObject object) throws ValidateException, UtilityException {
        ValidateUtils.notNull("exportAll", object);
        exportAll0(object, baseDirectory);
    }

    protected void exportAll0(ExtractObject object, Path baseDirectory) throws UtilityException {
        export(object, baseDirectory);

        if (object.hasAttachment()) {
            String baseSource = object.getSource();

            for (ExtractObject item : object.getAttachment()) {
                String itemSource = item.getSource().replaceAll(baseSource, StringUtils.EMPTY);
                Path itemDirectory = StringUtils.isEmpty(itemSource) ? baseDirectory : PathUtils.createDirectories(
                        baseDirectory.resolve(itemSource));
                exportAll0(item, itemDirectory);
            }
        }
    }

    protected String toPathName(ExtractObject object) {
        return StringUtils.isTrimEmpty(object.getName()) ? object.getId() : object.getName();
    }

    public String toJSON(Metadata metadata, boolean prettyFormat) {
        if (metadata == null || metadata.size() == 0) {
            return JSONUtils.EMPTY_JSON;
        }
        String[] names = metadata.names();
        TupleObject tmp = TupleObjectHelper.newObject(names.length);

        for (String item : names) {
            tmp.put(item, metadata.getValues(item));
        }
        return JSONUtils.toJSON(tmp, prettyFormat);
    }

    public void export(Metadata metadata, Path path) throws ValidateException, UtilityException {
        ValidateUtils.notNull("export", metadata, path);
        String json = toJSON(metadata,true);
        PathUtils.copyToPath(json.getBytes(), path);
    }

    public Path getBaseDirectory() {
        return baseDirectory;
    }

    public ExportProcessor setBaseDirectory(Path baseDirectory) throws UtilityException {
        if (baseDirectory != null) {
            PathUtils.createDirectories(baseDirectory);
            this.baseDirectory = baseDirectory;
        }
        return this;
    }

    public boolean getIncludeMetadata() {
        return includeMetadata;
    }

    public ExportProcessor setIncludeMetadata(boolean includeMetadata) {
        this.includeMetadata = includeMetadata;
        return this;
    }
}
