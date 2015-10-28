package r01f.xmlproperties;

/**
 * Informa de que se ha cargado un nuevo componente.
 */
interface XMLPropertiesComponentLoadedListener {
	/**
	 * Se ha cargado un nuevo componente.
	 * @param def La definici�n del componente
	 */
	public void newComponentLoaded(XMLPropertiesComponentDef def);
}
