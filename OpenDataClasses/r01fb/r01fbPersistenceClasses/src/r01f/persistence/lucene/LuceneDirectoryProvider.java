package r01f.persistence.lucene;
import java.io.File;
import java.io.IOException;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import r01f.types.Path;


/**
 * Provider de la implementacion {@link Directory} basada en:
 * <ul>
 * 		<li>FileSystem</li>
 * 		<li>JdbDirectory (solo Lucene 4)</li>
 * </ul>
 */
public class LuceneDirectoryProvider {
/////////////////////////////////////////////////////////////////////////////////////////
//	FileSystem
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Construye una implementaci�n de {@link Directory} basado en FileSystem
	 * (los �ndices de Lucene se guardan en FileSystem)
	 * @param indexFilesPath path donde se almacenan los �ndices de Lucene
	 * @return
	 */
	public Directory provideFileSystemDirectory(final Path indexFilesPath) throws IOException {
		return this.provideFileSystemDirectory(indexFilesPath.asString());
	}
	/**
	 * Construye una implementaci�n de {@link Directory} basado en FileSystem
	 * (los �ndices de Lucene se guardan en FileSystem)
	 * @param indexFilesPath path donde se almacenan los �ndices de Lucene
	 * @return
	 */
	@SuppressWarnings("static-method")
	public Directory provideFileSystemDirectory(final String indexFilesPath) throws IOException {
		Directory outDir = FSDirectory.open(new File(indexFilesPath));
		return outDir;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	Jdbc (solo Lucene 3)
/////////////////////////////////////////////////////////////////////////////////////////
//	/**
//	 * Construye una implementaci�n de {@link Directory} basada en BBDD
//	 * (los �ndices de Lucene se guardan en BBDD)
//	 * <b>IMPORTANTE!</b>
//	 * Utiliza la librer�a Commet para Lucene que se dej� de evolucionar en 2009 y 
//	 * por lo tanto NO soporta Lucene4
//	 * @param dataSource DataSource con la BBDD
//	 * @param dialect dialecto de la BBDD (oracle/mySql)
//	 * @param tableName nombre de la tabla donde se almacena el �ndice de Lucene
//	 * @return la implementaci�n de {@link Directory} basada en BBDD
//	 */
//	public Directory provideJdbcDirectory(final DataSource dataSource,
//										  final Dialect dialect,
//										  final String tableName) throws IOException {
//		return new LuceneJdbcDirectory(dataSource,
//									   dialect,
//									   tableName);
//	}
//	import java.io.IOException;
//	
//	import javax.inject.Inject;
//	import javax.sql.DataSource;
//	
//	import org.apache.lucene.store.Directory;
//	import org.apache.lucene.store.jdbc.JdbcDirectory;
//	import org.apache.lucene.store.jdbc.JdbcDirectorySettings;
//	import org.apache.lucene.store.jdbc.JdbcStoreException;
//	import org.apache.lucene.store.jdbc.dialect.Dialect;
//	import org.apache.lucene.store.jdbc.support.JdbcTable;
//	
//	/**
//	 * Implementaci�n de {@link Directory} de Lucene para BBDD
//	 * basada en Compass (ver http://mprabhat.wordpress.com/2012/08/13/create-lucene-index-in-database-using-jdbcdirectory/)
//	 * 
//	 * Esta clase es necesaria ya que hay que implementar algunos m�todos de {@link Directory} (lucene) que NO se
//	 * implementan en {@link JdbcDirectory} (Compass)
//	 * 
//	 * <b>IMPORTANTE!!!</b>
//	 * La implementaci�n de {@link Directory} para BBDD de Compass SOLO funciona para Lucene 3,
//	 * es decir, NO funciona para Lucene 4
//	 */
//	public class LuceneJdbcDirectory 
//	     extends JdbcDirectory {
//	/////////////////////////////////////////////////////////////////////////////////////////
//	//	ESTADO INYECTADO
//	/////////////////////////////////////////////////////////////////////////////////////////
//		/**
//		 * DataSource (conexi�n con la BBDD)
//		 */
//		@Inject
//		private DataSource _dataSource;
//	/////////////////////////////////////////////////////////////////////////////////////////
//	//	CONSTRUCTORES
//	/////////////////////////////////////////////////////////////////////////////////////////
//		public LuceneJdbcDirectory(final DataSource dataSource,final Dialect dialect,
//								   final String tableName) {
//			super(dataSource, dialect, tableName);
//		}
//		public LuceneJdbcDirectory(final DataSource dataSource,final Dialect dialect,final JdbcDirectorySettings settings,
//								   final String tableName) {
//			super(dataSource, dialect, settings, tableName);
//		}
//		public LuceneJdbcDirectory(final DataSource dataSource,final JdbcDirectorySettings settings,
//								   final String tableName) throws JdbcStoreException {
//			super(dataSource, settings, tableName);
//		}
//		public LuceneJdbcDirectory(final DataSource dataSource,
//								   final JdbcTable table) {
//			super(dataSource, table);
//		}
//		public LuceneJdbcDirectory(final DataSource dataSource,
//								   final String tableName)throws JdbcStoreException {
//			super(dataSource, tableName);
//		}
//	/////////////////////////////////////////////////////////////////////////////////////////
//	//	METODOS OVERRIDEN
//	/////////////////////////////////////////////////////////////////////////////////////////
//		@Override
//		public String[] listAll() throws IOException {
//			return super.list();		// Simply delegate
//		}
//	}
	
//	import org.apache.lucene.store.Directory;
//	import org.apache.lucene.store.jdbc.JdbcDirectory;
//	import org.apache.lucene.store.jdbc.dialect.Dialect;
//	import org.apache.lucene.store.jdbc.dialect.MySQLDialect;
//	import org.apache.lucene.store.jdbc.dialect.OracleDialect;
//	
//	import r01f.exceptions.Throwables;
//	import r01f.types.enums.Enums;
//	
//	/**
//	 * Factor�a de instancias de {@link Dialect} para la implementaci�n Compass de Lucene {@link Directory}
//	 * ({@link JdbcDirectory})
//	 */
//	public class LuceneJdbcDirectoryDialectProvider {
//	/////////////////////////////////////////////////////////////////////////////////////////
//	//	
//	/////////////////////////////////////////////////////////////////////////////////////////
//		/**
//		 * Devuelve el {@link Dialect} apropiado para el {@link JdbcDirectory}
//		 * @param dialect
//		 * @return
//		 */
//		public Dialect provideDialect(final LuceneJdbcDirectoryDialect dialect) {
//			Dialect outDialect = null;
//			switch(dialect) {
//			case ORACLE:
//				outDialect = new OracleDialect();
//				break;
//			case MYSQL:
//				outDialect = new MySQLDialect();
//				break;
//			default:
//				outDialect = new MySQLDialect();
//				break;
//			}
//			return outDialect;
//		}
//		/**
//		 * Devuelve el {@link Dialect} apropiado para el {@link JdbcDirectory}
//		 * @param dialectStr
//		 * @return
//		 */
//		public Dialect provideDialect(final String dialectStr) {
//			LuceneJdbcDirectoryDialect dialect = Enums.of(LuceneJdbcDirectoryDialect.class)
//													  .fromName(dialectStr);
//			if (dialect == null) throw new IllegalArgumentException(Throwables.message("The Lucene JdbcDirectory Dialect provider '{}' is NOT valid. The valid options are {}",
//																					   dialectStr,LuceneJdbcDirectoryDialect.values()));
//			return this.provideDialect(dialect);
//		}
//	/////////////////////////////////////////////////////////////////////////////////////////
//	//	
//	/////////////////////////////////////////////////////////////////////////////////////////
//		private enum LuceneJdbcDirectoryDialect {
//			ORACLE,
//			MYSQL;
//		}
//	}
}
