package r01f.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Clase que modela la paginaci�n en un conjunto de resultados
 * Esencialmente es una lista que contiene el c�digo de los primeros elementos
 * de cada p�gina y el n�mero total de elementos encontrados. 
 * 
 * Para utilizar el paginador es imprescindible que:
 * <ol>
 * 		<li>Tener ANTES de componer el pager el n�mero TOTAL de resultados y los OIDs de cada elemento de resultado</li>
 * 		<li>Los resultados se carguen ORDENADOS por algun criterio arbitrario</li>
 * 		<li>Cuando se recuperen los elementos de una p�gina la query se haga utilizando el criterio de ordenaci�n anterior</li>
 * </ol>
 * El uso del page es el siguiente:
 * <pre class='brush:java'>
 * 		Pager<String> pager = new Pager(10,		// Elementos por p�gina
 * 										10);	// P�ginas por bloque
 * 		long totalResultsCount = _computeTotalResultsCount(query);
 * 		for (long i=0; i<totalResultsCount; i++) {
 * 			page.add(i)
 * 		}
 * </pre>
 */
@Accessors(prefix="_")
public class Pager<T>
  implements Serializable {
	
    private static final long serialVersionUID = -4966151337012633456L;
///////////////////////////////////////////////////////////////////////////////////////////
//  MIEMBROS
///////////////////////////////////////////////////////////////////////////////////////////        
    private List<PageFirstAndLastItemsOids<T>> _pages = null;	// Lista de p�ginas
    @Getter @Setter private int  _pageSize  = 10;				// N�mero de elementos por p�gina
    @Getter @Setter private int  _blockSize = 5;    			// N�mero de p�ginas en cada bloque (utilizado para barras de navegaci�n)
    @Getter 		private int _totalNumberOfItems;     		// N�mero total de elementos encontrados en la b�squeda
    @Getter 		private int _currentPageOrderNumber;        // La p�gina actualmente mostrada
    
    @NoArgsConstructor
    private class PageFirstAndLastItemsOids<U> {
    	U _firstItemOid;
    	U _lastItemOid;
    }
///////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTORS
///////////////////////////////////////////////////////////////////////////////////////////        
    /** 
     * Constructor Pager 
     */
    public Pager() throws IllegalArgumentException {
        super();
        _pageSize = 10;		// Por defecto el n�mero de elementos por p�gina es 10
        _blockSize = 5;		// Inicializar la p�gina actual
        _currentPageOrderNumber = 1;		// P�gina actual = 1
        _pages = new ArrayList<PageFirstAndLastItemsOids<T>>();	// Inicializar la lista que contiene los oids del primer item de cada p�gina
    }
    /**
     * Constructor del pager en base a el tama�o de la pagina
     * @param newPageSize El tama�o de la pagina
     * @throws IllegalArgumentException si el tama�o de la pagina es menor que cero
     */
    public Pager(final int newPageSize) throws IllegalArgumentException {
        this();
        if (newPageSize <= 0) throw new IllegalArgumentException("El numero de elementos en la pagina no puede ser menor que cero");
        _pageSize = newPageSize;
    }
    /**
     * Constructor en base al tama�o de la p�gina y al tama�o del bloque de p�ginas de la barra de navegaci�n
     * @param newPageSize
     * @param newBlockSize
     * @throws IllegalArgumentException si el tama�o de la pagina es menor que cero
     */
    public Pager(final int newPageSize,final int newBlockSize) throws IllegalArgumentException {
        this();
        if (newPageSize <= 0 || newBlockSize <= 0) throw new IllegalArgumentException("El numero de elementos en la pagina o el tama�o del bloque de p�ginas de la barra de navegaci�n, no puede ser menor que cero");
        _pageSize = newPageSize;
        _blockSize = newBlockSize;
    }
///////////////////////////////////////////////////////////////////////////////////////////
//  INTRODUCCION DE PAGINAS
///////////////////////////////////////////////////////////////////////////////////////////    
    /**
     * Mete un item en el registro de p�ginas
     * OJO!!!!! En realidad SOLO se guardan los oids del PRIMER ITEM y ULTIMO ITEM DE LA PAGINA, es decir, solo se introduce el item
     * 			si la parte decimal de (numeroTotalElementos/pageSize) es cero, lo que quiere decir que se est� en 
     * 			un registro m�ltiplo del tama�o de p�gina (primer registro de la p�gina)
     * @param itemOid  El oid del registro actual resultado de la b�squeda que se mete en el paginador
     */
    public void addItem(final T itemOid) throws IllegalArgumentException {
    	if (itemOid == null) throw new IllegalArgumentException("NO se puede insertar un itemOid=null en el pager!");
        if (_pages == null) _pages = new ArrayList<PageFirstAndLastItemsOids<T>>();
        PageFirstAndLastItemsOids<T> currPageItem = null;
        if ((_totalNumberOfItems % _pageSize) == 0) {
        	currPageItem = new PageFirstAndLastItemsOids<T>();
        	currPageItem._firstItemOid = itemOid;
        	currPageItem._lastItemOid = itemOid;
        	_pages.add(currPageItem); 	// Introducir el indice del primer elemento de la nueva pagina
        } else {
        	currPageItem = _pages.get(_pages.size()-1);
        	currPageItem._lastItemOid = itemOid;
        }
        _totalNumberOfItems++;
    }    
///////////////////////////////////////////////////////////////////////////////////////////
//  METODOS DE ACCESO AL TAMA�O DE PAGINA Y BLOQUE
///////////////////////////////////////////////////////////////////////////////////////////    
    /**
     * Sirve para crear barras de navegaci�n en las que las p�ginas se dividen en bloques. 
     * Ejemplo: Si se han encontrado 23 p�ginas de
     *          resultados se puede mostrar una barra de navegaci�n con marcadores a 5 p�ginas como la siguiente: 
     *                  Pg Ant 1 2 3 4 5 Pg Sig
     * A medida que se va paginando, la ventana de las p�ginas en la barra de navegaci�n se va desplazando: 
     *                  Pg Ant 6 7 8 9 10 Pg Sig 
     * Este m�todo devuelve un array con los n�meros de las p�ginas en el bloque actual 
     * NOTA: El bloque actual depende de la p�gina actual.
     * @return Un array con los n�meros de las p�ginas en el bloque actual
     */
    public int[] getCurrentBlockPageNumbers() {
        int numeroBloques = this.getBlockCount();           // N�mero total de bloques
        int bloqueActual = this.getCurrentBlockNumber();    // N�mero del bloque actual
        // Obtener el tama�o del bloque actual
        // Todos los bloques contienen un numero de marcadores igual a _blockSize, excepto el �ltimo
        // que puede contener menos marcadores:
        // [Numero total paginas] - [Numero de paginas en los bloques anteriores al ultimo]
        int currentBlockSize = _blockSize;
        if (bloqueActual == numeroBloques) currentBlockSize = (_pages.size() - (numeroBloques - 1) * _blockSize);
        // Devolver los n�meros de las p�ginas del bloque actual en un array
        int[] blockPages = new int[currentBlockSize];
        int j = 0;
        for (int i = 1; i <= blockPages.length; i++) blockPages[i - 1] = i;
        for (int i = ((bloqueActual - 1) * _blockSize + 1); ((i <= (bloqueActual * _blockSize)) && (i <= _pages.size())); i++) {
            blockPages[j++] = i; // Meter la p�gina en el array
        }
        return blockPages;
    }
    /**
     * Devuelve el n�mero del bloque actual
     * @return el n�mero del bloque actual
     */
    public int getCurrentBlockNumber() {
        // El bloque actual es la parte entera de [Pagina Actual-1] / [Tama�o Bloque] + 1
        int bloqueActual = (new Double(((_currentPageOrderNumber - 1) / _blockSize) + 1)).intValue(); // Los bloques empiezan por 1
        return bloqueActual;
    }
    /**
     * Devuelve el n�mero total de bloques de p�ginas
     * @return el n�mero de bloques
     */
    public int getBlockCount() {
        // Obtener el n�mero de bloques 
        // El n�mero de bloques es el primer entero mayor que [Numero total de paginas] / [Tama�o bloque]
        double dblPaginas = _pages.size();
        int numeroBloques = (new Double(Math.ceil(dblPaginas / _blockSize))).intValue();
        return numeroBloques;
    }
///////////////////////////////////////////////////////////////////////////////////////////
//  METODOS DE ACCESO A LOS VALORES DE LOS OIDs DE LOS PRIMEROS ELEMENTOS DE LA P�GINA
///////////////////////////////////////////////////////////////////////////////////////////    
    /**
     * Devuelve oid del item del primer elemento de la p�gina actual
     * @return C�digo del primer elemento de la p�gina actual
     */
    public T getCurrentPageFirstItemOid() {
        return this.getPageFirstItemOid(_currentPageOrderNumber);
    }
    /**
     * Devuelve el oid del item del ultimo elemento de la pagina actual
     * @return Codigo del ultimo elemento de la p�gina actual
     */
    public T getCurrentPageLastItemOid() {
        return this.getPageLastItemOid(_currentPageOrderNumber);
    }
    /**
     * Devuelve el valor del primer elemento de la p�gina anterior o null si se est� en la primera p�gina
     * @return Object
     */
    public T getPrevPageLastItemOid() {
        return _currentPageOrderNumber > 1 ? this.getPageLastItemOid(_currentPageOrderNumber-1) : null;
    }
    /**
     * Devuelve el valor del primer elemento de la p�gina siguiente o null si se est� en la �ltima p�agina
     * @return El c�digo del primer elemento de la siguiente p�gina
     */
    public T getNextPageFirstItemOid() {
    	return _currentPageOrderNumber < _pages.size() ? this.getPageFirstItemOid(_currentPageOrderNumber+1) : null;
    }
    /**
     * Devuelve el oid del primer item de una p�gina
     * @param inPage N�mero de la p�gina solicitada
     * @return C�digo del primer elemento de la p�gina que se indica
     */
    public T getPageFirstItemOid(final int inPage) throws IllegalArgumentException {
        if (inPage < 1 || inPage > _pages.size()) throw new IllegalArgumentException("P�gina ilegal");
        PageFirstAndLastItemsOids<T> pageFirstAndLastItems = _pages.get(inPage-1);
        return pageFirstAndLastItems._firstItemOid;
    }
    /**
     * Devuelve el oid del �ltimo item de una p�gina
     * @param inPage n�mero de la p�gina solicitada
     * @return c�digo del �ltimo elemento de la p�gina
     */
    public T getPageLastItemOid(final int inPage) throws IllegalArgumentException {
        if (inPage < 1 || inPage > _pages.size()) throw new IllegalArgumentException("P�gina ilegal");
        PageFirstAndLastItemsOids<T> pageFirstAndLastItems = _pages.get(inPage-1);
        return pageFirstAndLastItems._lastItemOid;	
    }
///////////////////////////////////////////////////////////////////////////////////////////
//  METODOS DE ACCESO A LOS N�MEROS DE ORDEN DE LOS ITEMS EN FUNCI�N DE LA P�GINA
///////////////////////////////////////////////////////////////////////////////////////////    
    /**
     * Devuelve el n�mero de orden de la primera p�gina
     * @return Siempre devuelve 1
     */
    @SuppressWarnings("static-method")
	public int getFirstPageOrderNumber() {
        return 1;
    }
    /**
     * Devuelve el n�mero de orden de la �ltima p�gina
     * @return el n�mero de pag�gina
     */
    public int getLastPageOrderNumber() {
        return _pages.size();
    }
    /**
     * Devuelve el n�mero de orden de la p�gina anterior o -1 si se est� en la primera p�gina
     * @return 
     */
    public int getPrevPageOrderNumber() {
    	if (_currentPageOrderNumber == 1) return -1;
    	return _currentPageOrderNumber - 1;
    }
    public int getNextPageOrderNumber() {
    	if (_currentPageOrderNumber == _pages.size()) return -1;
    	return _currentPageOrderNumber + 1;
    }
    /**
     * Devuelve el n�mero de orden del primer elemento de la p�gina 
     * Si por ejemplo hay 12 p�ginas de 10 elementos cada una, la p�gina 2
     * tendr� los elementos del 11 al 20. Este m�todo devolver� 11
     * @return el numero de orden del primer elemento de la pagina
     */
    public int getCurrentPageFirstItemOrderNumber() {
        return this.getPageFirstItemOrderNumber(_currentPageOrderNumber);
    }
    /**
     * Devuelve el n�mero de orden del ultimo elemento de la p�gina 
     * Si por ejemplo hay 12 p�ginas de 10 elementos cada una, la p�gina 2
     * tendr� los elementos del 11 al 20. Este m�todo devolver� 20
     * @return El numero de orden del ultimo elemento de la pagina 
     */
    public int getCurrentPageLastItemOrderNumber() {
        return this.getPageLastItemOrderNumber(_currentPageOrderNumber);
    }
    /**
     * Devuelve el n�mero de orden del elemento anterior al primero de la p�gina actual
     * (el �ltimo elemento de la p�gina anterior)
     * Si por ejemplo hay 12 p�ginas de 10 elemento cada una, la p�gina 2
     * tendr� los elementos del 11 al 20. Este m�todo devolver� 10
     * @return el n�mero de orden del elemento anterior al primero de la p�gina
     */
    public int getPreviousPageLastItemOrderNumber() {
    	if (_currentPageOrderNumber == 1) throw new IllegalArgumentException("P�gina Ilegal");
    	return this.getPageLastItemOrderNumber(_currentPageOrderNumber-1);
    }
    /**
     * Devuelve el n�mero de orden del elemento siguiente al primero de la p�gina actual
     * (el primer elemento de la p�gina siguiente)
     * Si por ejemplo hay 12 p�ginas de 10 elemento cada una, la p�gina 2
     * tendr� los elementos del 11 al 20. Este m�todo devolver� 21
     * @return el n�mero de orden del elemento siguiente al �ltimo de la p�gina
     */    
    public int getNextPageFirstItemOrderNumber() {
    	if (_currentPageOrderNumber == _pages.size()) throw new IllegalArgumentException("P�gina Ilegal");
    	return this.getPageFirstItemOrderNumber(_currentPageOrderNumber + 1);
    }
    /**
     * Obtiene el n�mero de orden del primer elemento de una p�gina
     * @param pageOrderNum n�mero de orden de la p�gina
     * @return 
     */
    public int getPageFirstItemOrderNumber(final int pageOrderNum) throws IllegalArgumentException {
        if (pageOrderNum < 1 || pageOrderNum > _pages.size()) throw new IllegalArgumentException("P�gina ilegal");
        if (pageOrderNum == this.getFirstPageOrderNumber()) return 1;
        return (pageOrderNum * _pageSize) - _pageSize + 1;
    }
    /**
     * Devuelve el n�mero de orden del �ltimo elemento de una p�gina
     * @param pageOrderNum n�mero de orden de la p�gina
     * @return
     */
    public int getPageLastItemOrderNumber(final int pageOrderNum) throws IllegalArgumentException {
        if (pageOrderNum < 1 || pageOrderNum > _pages.size()) throw new IllegalArgumentException("P�gina ilegal");
        if (pageOrderNum == this.getLastPageOrderNumber()) return this.getLastPageOrderNumber();
        return pageOrderNum * _pageSize;
    }
///////////////////////////////////////////////////////////////////////////////////////////
//  METODOS DE ACCESO A LOS NUMEROS DE ORDEN DE LOS ITEMS EN FUNCI�N DEL BLOQUE
///////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Devuelve el n�mero de orden de la �ltima p�gina del bloque anterior al actual
     * @return
     */
    public int getPrevBlockLastPageOrderNumber() {
        int currentBlockPageNums[] = this.getCurrentBlockPageNumbers();
        int prevBlockLastPage = currentBlockPageNums[0] == 1 ? 1 : currentBlockPageNums[0]-1;
        return prevBlockLastPage;
    }
    /**
     * Devuelve el n�mero de orden de la primera p�gina del bloque siguiente al actual
     * @return
     */
    public int getNextBlockFirstPageOrderNumber() {
        int currentBlockPageNums[] = this.getCurrentBlockPageNumbers();
        int nextBlockFirstPage = currentBlockPageNums[currentBlockPageNums.length-1] == _pages.size() ? currentBlockPageNums[currentBlockPageNums.length-1]
        																							  : currentBlockPageNums[currentBlockPageNums.length-1]+1;
        return nextBlockFirstPage;
    }
    /**
     * Devuelve el n�mero de orden del �ltimo elemento del bloque anterior al bloque actual.
     * Si por ejemplo hay 25 p�ginas de 10 elementos cada una y se est� en el segundo bloque
     * (de la p�gina 11 a la 25), este m�todo devolver� 10 (el n�mero de orden del �ltimo
     * elemento de la p�gina anterior)
     * @return el n�mero de orden del �ltimo elememento del bloque anterior.
     */
    public int getPrevBlockLastItemOrderNumber() {
        int prevBlockLastPageOrderNumber = this.getPrevBlockLastPageOrderNumber();
        return this.getPageLastItemOrderNumber(prevBlockLastPageOrderNumber);
    }
    /**
     * Devuelve el n�mero de orden del �ltimo elemento del bloque anterior al 
     * bloque actual.
     * Si por ejemplo hay 25 p�ginas de 10 elementos cada una y se est� en el segundo bloque
     * (de la p�gina 11 a la 25), este m�todo devolver� 10 (el n�mero de orden del �ltimo
     * elemento de la p�gina anterior)
     * @return el n�mero de orden del �ltimo elememento del bloque anterior.
     */
    public int getNextBlockFirstItemOrderNumber() {
        int nextBlockFirstPageOrderNumber = this.getNextBlockFirstPageOrderNumber();
        return this.getPageFirstItemOrderNumber(nextBlockFirstPageOrderNumber);
    }    
///////////////////////////////////////////////////////////////////////////////////////////
//  CUENTA DE ELEMENTOS
///////////////////////////////////////////////////////////////////////////////////////////        
    /**
     * Devuelve el n�mero de p�ginas de la b�squeda
     * @return El n�mero de p�ginas encontradas
     */
    public int getPageCount() {
        return _pages.size();
    }
    
///////////////////////////////////////////////////////////////////////////////////////////
//  MOVIMIENTO ENTRE PAGINAS
///////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Establece el puntero a la primera p�gina
     */
    public void goToFirstPage() {
        _currentPageOrderNumber = 1;
    }
    /**
     * Establece el puntero en la �ltima p�gina
     */
    public void goToLastPage() {
        _currentPageOrderNumber = _pages.size();
    } 
    /**
     * Establece el puntero en la p�gina anterior
     */
    public void goToPrevPage() {
        if (_currentPageOrderNumber == 1) return; 	// Comprobar que la nueva p�gina no es mayor que el n�mero total de p�ginas
        _currentPageOrderNumber--;
    }
    /**
     * Establece el puntero en la siguiente p�gina
     */
    public void goToNextPage() {
        if (_currentPageOrderNumber == _pages.size()) return;	// Comprobar que la nueva p�gina no es mayor que el n�mero total de p�ginas
        _currentPageOrderNumber++;
    }
    /**
     * Establece el puntero en la p�gina indicada
     * @param newPage N�mero de la nueva p�gina a la que hay que moverse
     */
    public void goToPage(int newPage) throws IllegalArgumentException {       
        if (newPage < 0 || newPage > _pages.size()) throw new IllegalArgumentException("P�gina ilegal");
        _currentPageOrderNumber = newPage;
    } 
    /**
     * Mueve el puntero al ultimo elemento del bloque anterior de resultados
     */
    public void goToPrevBlockLastPage() {
        _currentPageOrderNumber = this.getPrevBlockLastPageOrderNumber();
    }
    /**
     * Mueve el puntero al primer elemento del bloque siguiente de resultados
     */
    public void goToNextBlockFirstPage() {
        _currentPageOrderNumber = this.getNextBlockFirstPageOrderNumber();
    }
}
