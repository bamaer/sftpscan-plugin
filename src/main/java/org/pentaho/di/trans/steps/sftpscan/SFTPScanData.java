package org.pentaho.di.trans.steps.sftpscan;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * The SFTP Scan Plugin Data Class
 */
public class SFTPScanData extends BaseStepData implements StepDataInterface {

    public RowMetaInterface inputRowMeta;
    public RowMetaInterface outputRowMeta;

    public SFTPScanData() {
        super();
    }
}
