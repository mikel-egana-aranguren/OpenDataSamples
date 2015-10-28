package euskadi.opendata.model;

import java.io.Serializable;
import java.util.Collection;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import r01f.util.types.collections.CollectionUtils;

/**
 * Procurement data for an entity
 */
@Accessors(prefix="_") 
public class ProcurementDataForEntity 
  implements Serializable {

	private static final long serialVersionUID = -6396015892238180722L;
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Getter @Setter private String _entityOid;
	@Getter @Setter private String _entityName;
	
	@Getter @Setter private Collection<ProcurementData> _procurements;
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Sums all procurements values
	 * @return
	 */
	public double sumProcurements() {
		double outSum = 0;
		if (CollectionUtils.hasData(_procurements)) {
			for (ProcurementData proc : _procurements) {
				outSum = outSum + proc.getValue();
			}
		}
		return outSum;
	}
	/**
	 * Gets the highest valued procurement
	 * @return
	 */
	public ProcurementData highestValueProcurement() {
		ProcurementData outProc = null;
		if (CollectionUtils.hasData(_procurements)) {
			for (ProcurementData proc : _procurements) {
				if (outProc == null || proc.getValue() > outProc.getValue()) outProc = proc;
			}
		}
		return outProc;
	}
}
