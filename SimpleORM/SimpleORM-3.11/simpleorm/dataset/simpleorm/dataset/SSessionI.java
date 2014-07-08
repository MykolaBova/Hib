package simpleorm.dataset;

/**
 * Used to link to the SSession from SDataSet, and thus record instances etc.
 * This is used by findReference to lazily load refrences.  
 * But the link is null if the dataset is detached.<p>
 *
 * @author aberglas
 */
public abstract class SSessionI {
  public abstract <RI extends SRecordInstance> RI findOrCreate(SRecordMeta<RI> rmeta, SFieldScalar[] selectList, SQueryMode queryMode, Object... keys);
  public abstract <RI extends SRecordInstance> RI find(SRecordMeta<RI> rmeta, SFieldScalar[] selectList, SQueryMode queryMode, Object... keys);
}
