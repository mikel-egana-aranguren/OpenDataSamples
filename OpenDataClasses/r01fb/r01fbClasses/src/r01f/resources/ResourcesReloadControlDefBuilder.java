package r01f.resources;

import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.regex.Matcher;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import r01f.guids.CommonOIDs.Password;
import r01f.guids.CommonOIDs.UserCode;
import r01f.patterns.IsBuilder;
import r01f.resources.ResourcesLoaderDef.ResourcesLoaderType;
import r01f.resources.ResourcesReloadControlDef.ResourcesReloadPolicy;
import r01f.types.Path;
import r01f.types.TimeLapse;
import r01f.util.types.Strings;

public class ResourcesReloadControlDefBuilder 
  implements IsBuilder {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * @return a void reloading control def
	 */
	public static ResourcesReloadControlDef createForVoidReloading() {
		ResourcesReloadControlDef ctrlDef = new ResourcesReloadControlDef(ResourcesReloadPolicy.VOID);
		ctrlDef.setCheckInterval(Long.toString(ResourceBundle.Control.TTL_NO_EXPIRATION_CONTROL));
		return ctrlDef;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * @return a NOT reloading control def
	 */
	public static ResourcesReloadControlDef createForNOTReloading() {
		ResourcesReloadControlDef ctrlDef = new ResourcesReloadControlDef(ResourcesReloadPolicy.NO_RELOAD);
		ctrlDef.setCheckInterval(Long.toString(ResourceBundle.Control.TTL_NO_EXPIRATION_CONTROL));
		return ctrlDef;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * <pre class='brush:java'>
	 * 		ResourcesReloadControlDef def = ResourcesReloadControlDef.createForPeriodicReloading()
	 * 																 .reloadingEvery(1000)
	 * 																 .enabled();	
	 * </pre>
	 * @return a builder for a periodic reloading
	 */
	public static ResourcesReloadControlBuilderPeriodicIntervalStep createForPeriodicReloading() {
		ResourcesReloadControlDef outDef = new ResourcesReloadControlDef(ResourcesReloadPolicy.PERIODIC);
		return new ResourcesReloadControlBuilderPeriodicIntervalStep(outDef);
	}
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public static class ResourcesReloadControlBuilderPeriodicIntervalStep {
		private final ResourcesReloadControlDef _reloadControlDef;
		
		public ResourcesReloadControlBuilderEnabledStep reloadingEvery(final long milis) {
			_reloadControlDef.setControlProps(new HashMap<String,String>(1));
			_reloadControlDef.getControlProps()
							 .put(ResourcesReloadControlPeriodic.PERIOD_PROP_KEY,Long.toString(milis));
			return new ResourcesReloadControlBuilderEnabledStep(_reloadControlDef);
		}
		public ResourcesReloadControlBuilderEnabledStep reloadingEvery(final String timeSpec) {
			long millis = TimeLapse.createFor(timeSpec)
								   .asMilis();
			return this.reloadingEvery(millis);
		}
		public ResourcesReloadControlBuilderEnabledStep reloadingEvery(final TimeLapse period) {
			long millis = period.asMilis();
			return this.reloadingEvery(millis);
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * <pre class='brush:java'>
	 * 		ResourcesReloadControlDef def = ResourcesReloadControlDef.createForFileUpdateTimeStamp()
	 * 																 .forFile(Path.of("/config/r01/r01.properties.xml")
	 * 																 .loadedUsing(ResourcesLoaderType.CLASSPATH)
	 * 																 .checkingIfReloadIsNeededEvery(1000)
	 * 																 .enabled();	
	 * </pre>
	 * @return a builder for a file update timestamp reloading
	 */
	public static ResourcesReloadControlBuilderFileUpdateTimeStampStep createForFileUpdateTimeStamp() {
		ResourcesReloadControlDef outDef = new ResourcesReloadControlDef(ResourcesReloadPolicy.FILE_LAST_MODIF_TIMESTAMP);
		outDef.setControlProps(new HashMap<String,String>());
		return new ResourcesReloadControlBuilderFileUpdateTimeStampStep(outDef);
	}
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public static class ResourcesReloadControlBuilderFileUpdateTimeStampStep {
		private final ResourcesReloadControlDef _reloadControlDef;
		
		public ResourcesReloadControlBuilderFileUpdateTimeStampLoaderStep forFile(final Path path) {
			_reloadControlDef.getControlProps()
							 .put(ResourcesReloadControlFileLastModifTimeStampBased.FILETOCHECK_PROP_KEY,path.asString());
			return new ResourcesReloadControlBuilderFileUpdateTimeStampLoaderStep(_reloadControlDef);
		}
		public ResourcesReloadControlBuilderCheckStep forFileLoadedAs(final String loaderAndPath) {
			if (Strings.isNullOrEmpty(loaderAndPath)) throw new IllegalArgumentException(Strings.of("The filePath is mandatory for the {}")
																							    .customizeWith(ResourcesReloadControlFileLastModifTimeStampBased.class.getName())
																							    .asString());
			Matcher m = ResourcesReloadControlFileLastModifTimeStampBased.FILEPATH_PATTERN.matcher(loaderAndPath);
			if (m.find()) {
				if (m.groupCount() > 1) {
					_reloadControlDef.getControlProps()
									 .put(ResourcesReloadControlFileLastModifTimeStampBased.FILETOCHECKLOADERTYPE_PROP_KEY,m.group(1));
					_reloadControlDef.getControlProps()
									 .put(ResourcesReloadControlFileLastModifTimeStampBased.FILETOCHECK_PROP_KEY,m.group(2));
				} else {
					_reloadControlDef.getControlProps()
									 .put(ResourcesReloadControlFileLastModifTimeStampBased.FILETOCHECK_PROP_KEY,m.group(1));
				}
			}
			return new ResourcesReloadControlBuilderCheckStep(_reloadControlDef);
		}
	}
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public static class ResourcesReloadControlBuilderFileUpdateTimeStampLoaderStep {
		private final ResourcesReloadControlDef _reloadControlDef;
		
		public ResourcesReloadControlBuilderCheckStep loadedUsing(final ResourcesLoaderType resLoader) {
			_reloadControlDef.getControlProps()
							 .put(ResourcesReloadControlFileLastModifTimeStampBased.FILETOCHECKLOADERTYPE_PROP_KEY,resLoader.name());
			return new ResourcesReloadControlBuilderCheckStep(_reloadControlDef);
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * <pre class='brush:java'>
	 * 		ResourcesReloadControlDef def = ResourcesReloadControlDef.createForBBDDChecking()
	 * 																 .conectingUsingDataSource("myDataSource")
	 * 																 .sqlToLoadReloadFlag(...)
	 * 																 .sqlToUpdateReloadFlag(...)
	 * 																 .checkingIfReloadIsNeededEvery(1000)
	 * 																 .enabled();	
	 * </pre>
	 * @return a builder for a file update timestamp reloading
	 */
	public static ResourcesReloadControlBuilderBBDDLoaderPropertiesConxStep createForBBDDChecking() {
		ResourcesReloadControlDef reloadCtrlDef = new ResourcesReloadControlDef(ResourcesReloadPolicy.BBDD);
		return new ResourcesReloadControlBuilderBBDDLoaderPropertiesConxStep(reloadCtrlDef);
	}
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public static class ResourcesReloadControlBuilderBBDDLoaderPropertiesConxStep {
		private final ResourcesReloadControlDef _reloadControlDef;
		
		public ResourcesReloadControlBuilderBBDDLoaderPropertiesLoadSqlStep conectingUsingDataSource(final String dataSourceName) {
			_reloadControlDef.setControlProps(new HashMap<String,String>(4));
			_reloadControlDef.getControlProps().put(ResourcesLoaderFromBBDD.CLASS,"DataSource");
			_reloadControlDef.getControlProps().put(ResourcesLoaderFromBBDD.URI,dataSourceName);
			return new ResourcesReloadControlBuilderBBDDLoaderPropertiesLoadSqlStep(_reloadControlDef);
		}
		public ResourcesReloadControlBuilderBBDDLoaderPropertiesLoadSqlStep conectingUsing(final String driverClass,
																						   final String conxUri,
																						   final UserCode user,final Password password) {
			_reloadControlDef.setControlProps(new HashMap<String,String>(6));
			_reloadControlDef.getControlProps().put(ResourcesLoaderFromBBDD.CLASS,driverClass);
			_reloadControlDef.getControlProps().put(ResourcesLoaderFromBBDD.URI,conxUri);
			_reloadControlDef.getControlProps().put(ResourcesLoaderFromBBDD.USER,user.asString());
			_reloadControlDef.getControlProps().put(ResourcesLoaderFromBBDD.PASSWORD,password.asString());
			return new ResourcesReloadControlBuilderBBDDLoaderPropertiesLoadSqlStep(_reloadControlDef);
		}		
	}
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public static class ResourcesReloadControlBuilderBBDDLoaderPropertiesLoadSqlStep {
		private final ResourcesReloadControlDef _reloadControlDef;
		
		public ResourcesLoaderDefManualBuilderBBDDLoaderPropertiesUpdateTSSqlStep sqlToLoadReloadFlag(final String sql) {
			_reloadControlDef.getControlProps().put(ResourcesReloadControlBBDDFlagBased.RELOADFLAGQUERYSQL_PROP,sql);
			return new ResourcesLoaderDefManualBuilderBBDDLoaderPropertiesUpdateTSSqlStep(_reloadControlDef);
		}
		public ResourcesLoaderDefManualBuilderBBDDLoaderPropertiesUpdateTSSqlStep defaultSqlToReloadFlag() {
			return this.sqlToLoadReloadFlag(ResourcesReloadControlBBDDFlagBased.DEFAULT_LOAD_FLAG_SQL);
		}
	}
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public static class ResourcesLoaderDefManualBuilderBBDDLoaderPropertiesUpdateTSSqlStep {
		private final ResourcesReloadControlDef _reloadControlDef;
		
		public ResourcesReloadControlBuilderCheckStep sqlToUpdateReloadFlag(final String sql) {
			_reloadControlDef.getControlProps().put(ResourcesReloadControlBBDDFlagBased.RELOADFLAGUPDATESQL_PROP,sql);
			return new ResourcesReloadControlBuilderCheckStep(_reloadControlDef);
		}
		public ResourcesReloadControlBuilderCheckStep defaultSqlToUpdateReloadFlag() {
			return this.sqlToUpdateReloadFlag(ResourcesReloadControlBBDDFlagBased.DEFAULT_UPDATE_FLAG_SQL);
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public static class ResourcesReloadControlBuilderCheckStep {
		private final ResourcesReloadControlDef _reloadControlDef;
		/**
		 * Sets the milis interval to check if a reload is needed
		 * @param millis the interval in milliseconds
		 */
		public ResourcesReloadControlBuilderEnabledStep checkingIfReloadIsNeededEvery(final long milis) {
			_reloadControlDef.setCheckInterval(Long.toString(milis));
			return new ResourcesReloadControlBuilderEnabledStep(_reloadControlDef);
		}
		/**
		 * Sets the milis interval to check if a reload is needed
		 * @param millis the interval in milliseconds
		 */
		public ResourcesReloadControlBuilderEnabledStep checkingIfReloadIsNeededEvery(final String intervalSpec) {
			_reloadControlDef.setCheckInterval(TimeLapse.createFor(intervalSpec)
														.asString());
			return new ResourcesReloadControlBuilderEnabledStep(_reloadControlDef);
		}
		/**
		 * Sets the milis interval to check if a reload is needed
		 * @param millis the interval in milliseconds
		 */
		public ResourcesReloadControlBuilderEnabledStep checkingIfReloadIsNeededEvery(final TimeLapse intervalSpec) {
			_reloadControlDef.setCheckInterval(intervalSpec.asString());
			return new ResourcesReloadControlBuilderEnabledStep(_reloadControlDef);
		}
	}
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public static class ResourcesReloadControlBuilderEnabledStep {
		private final ResourcesReloadControlDef _reloadControlDef;
		
		public ResourcesReloadControlDef enabled() {
			_reloadControlDef.setEnabled(true);
			return _reloadControlDef;
		}
		public ResourcesReloadControlDef disabled() {
			_reloadControlDef.setEnabled(false);
			return _reloadControlDef;
		}
	}
}
