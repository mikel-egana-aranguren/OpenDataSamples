
package r01f.util.types;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;

import r01f.locale.Language;

/**
 * Date utils
 * (see http://www.odi.ch/prog/design/datetime.php)
 */
public abstract class Dates {
///////////////////////////////////////////////////////////////////////////////////////////
//  CONSTANTS
///////////////////////////////////////////////////////////////////////////////////////////
    public static final String ES_DEFAULT_FORMAT = "dd/MM/yyyy";
    public static final String EU_DEFAULT_FORMAT = "yyyy/MM/dd";
    public static final String EPOCH = "MMM dd yyyy HH:mm:ss.SSS zzz";
    public static final String DEFAULT_FORMAT = ES_DEFAULT_FORMAT;
    public static final String ISO8601 = "yyyy-MM-dd'T'HH:mm'Z'";
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
    /**
     * @return the time now
     */
    public static Date now() {
    	return Calendar.getInstance().getTime();
    }
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Checks if an object is a java.util.Date or a java.sql.Date
     * @param obj
     * @return
     */
    public static <T> boolean isDate(final T obj) {
    	return obj instanceof java.util.Date || obj instanceof java.sql.Date;
    }
/////////////////////////////////////////////////////////////////////////////////////////
//  EPOCH see http://www.epochconverter.com/
/////////////////////////////////////////////////////////////////////////////////////////
    /**
     * @return the epoch time
     */
    public static long epochTimeStamp() {
    	return System.currentTimeMillis();
    }
    /**
     * Returns an epoch timestamp as a String human readable like "MMM dd yyyy HH:mm:ss.SSS zzz"
     * @param epochTimeStamp
     * @return
     */
    public static String epochTimeStampAsString(long epochTimeStamp) {
    	String date = new SimpleDateFormat(Dates.EPOCH)
    							.format(new Date(epochTimeStamp*1000));
    	return date;
    }
    /**
     * Returns an epoch timetamp from it's human radable representation like "MMM dd yyyy HH:mm:ss.SSS zzz"
     * @param epochTimeStampAsString
     * @return
     */
    public static long epochTimeStampFromString(final String epochTimeStampAsString) {
    	long epoch = 0;
    	try {
	    	epoch = new SimpleDateFormat(Dates.EPOCH)
	    						.parse(epochTimeStampAsString).getTime() / 1000;
    	} catch(ParseException parseEx) {
    		parseEx.printStackTrace(System.out);
    	}
    	return epoch;
    }
///////////////////////////////////////////////////////////////////////////////////////////
//  AUX METODOS
///////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Gets the date format pattern depending on the provided language
     * @param lang the language
     * @param langFormats a map with the language patterns
     * @return 
     */
    public static String langFormat(final Language lang,final Map<Language,String> langFormats) {
        String fmt = null;
        if (langFormats != null) {
            if (lang != null) fmt = langFormats.get(lang);
            if (fmt == null) fmt = langFormats.get(Language.DEFAULT);
            if (fmt == null) fmt = langFormats.get(Language.ENGLISH);	// english by default 
        }
        if (fmt == null) fmt = DEFAULT_FORMAT;
        return fmt;
    }
///////////////////////////////////////////////////////////////////////////////////////////
//  CONVERSION METHODS
///////////////////////////////////////////////////////////////////////////////////////////
    /**
     *
     * Returns the Date as milis
     * @param date
     * @return 
     */
    public static long asMillis(final Date date) {
        return Dates.asEpochTimeStamp(date);
    }
    /**
     * Return a Date as an epoch timeStamp
     * @param date
     * @return
     */
    public static long asEpochTimeStamp(final Date date) {
    	if (date == null) return Long.MIN_VALUE;
    	return date.getTime();
    }
    /**
     * Returns the Date as a Calendar
     * @param date the Date
     * @return the returned Calendar
     */
    public static GregorianCalendar asCalendar(final Date date) {
    	if (date == null) return null;
    	GregorianCalendar outCal = new GregorianCalendar();
    	outCal.setTime(date);
    	return outCal;
    }
    /**
     * Returns the Date as a {@link Timestamp}
     * @param date
     * @return
     */
    public static Timestamp asSqlTimestamp(final Date date) {
    	if (date == null) return null;
    	Timestamp outTS = new Timestamp(date.getTime());
    	return outTS;
    }
    /**
     * Returns a date from it's milis representation
     * @param milis formato num�rico
     * @return string dd/mm/yyyy
     */
    public static Date fromMillis(final long milis) {
        return Dates.fromEpochTimeStamp(milis);
    }
    /**
     * Returns a date from it's epoch timestamp representation
     * @param epochTimeStamp
     * @return
     */
    public static Date fromEpochTimeStamp(final long epochTimeStamp) {
    	return new Date(epochTimeStamp);
    }
    /**
     * Returns a {@link Date} from a calendar
     * @param cal
     * @return
     */
    public static Date fromCalendar(final Calendar cal) {
    	if (cal == null) return null;
    	return cal.getTime();
    }
///////////////////////////////////////////////////////////////////////////////////////////
//  FORMAT METHODS
///////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Returns a date formated as ISO8601 (yyyy-MM-dd'T'HH:mm'Z') GMT (greenwich meridian time) / UTC (coordinate universal time) time
     * (see http://www.timeanddate.com/time/gmt-utc-time.html and http://stackoverflow.com/questions/3914404/how-to-get-current-moment-in-iso-8601-format
     *      http://www.odi.ch/prog/design/datetime.php)
     * @param date
     * @return
     */
    public static String formatAsISO8601(final Date date) {
    	TimeZone tz = TimeZone.getTimeZone("UTC");
    	DateFormat df = new SimpleDateFormat(Dates.ISO8601);
    	df.setTimeZone(tz);
    	String outISODate = df.format(date);
    	return outISODate;
    }
    public static String formatAsUTC(final Date date) {
    	return Dates.formatAsISO8601(date);
    }
    /**
     * Returns a date formated as epoch default format "MMM dd yyyy HH:mm:ss.SSS zzz"
     * @param date
     * @return
     */
    public static String formatAsEpochTimeStamp(final Date date) {
    	return Dates.epochTimeStampAsString(date.getTime());
    }
    /**
     * Gets the Date formated
     * The format pattern can contain
     * <pre>
     *      y -> Year
     *      M -> Month
     *      d -> Day
     * </pre>
     * It's also possible to return the milis formated date if the format param = milis
     * @param date 
     * @param fmt 
     * @return 
     */
    public static String format(final Date date,final String fmt) {

        return Dates.format(date,fmt,Locale.getDefault());
    }
    /**
     * Formats a milis given date
     * @param milis
     * @param fmt
     * @return
     */
    public static String format(final long milis,final String fmt) {
    	Date date = Dates.fromMillis(milis);
    	return Dates.format(date,fmt);
    }
    /**
     * Gets the Date formated
     * The format pattern can contain
     * <pre>
     *      y -> Year
     *      M -> Month
     *      d -> Day
     * </pre>
     * @param date 
     * @param fmt 
     * @param locale format language. For Locale.English the timeZone is set to GMT (RSS uses this).
     * @return 
     */
    public static String format(final Date date,final String fmt,
    							final Locale locale) {
        Date theDate = date != null ? date : new Date();
        
        String theFmt = Strings.isNullOrEmpty(fmt) ? DEFAULT_FORMAT : fmt; 		// Dates default format
        boolean isISO = theFmt.equalsIgnoreCase("iso") 
        			 || theFmt.equalsIgnoreCase("iso8601") 
        			 || theFmt.equalsIgnoreCase("utc");
        
        if (theFmt.equalsIgnoreCase("millis") || theFmt.equalsIgnoreCase("milis")) {	// millis bug WTF!
            return Long.toString(theDate.getTime());
        } else if (theFmt.equalsIgnoreCase("seconds")) {
            return Long.toString(theDate.getTime() / 1000L);
        } else if (theFmt.equalsIgnoreCase("epoch")) {
        	theFmt = Dates.EPOCH;		// "MMM dd yyyy HH:mm:ss.SSS zzz"
        } else if (isISO) {
        	theFmt = Dates.ISO8601;
        }
        
        SimpleDateFormat formatter = new SimpleDateFormat(theFmt,locale);
        // Adjust to UTC for ISO time
        if (isISO) formatter.setTimeZone(TimeZone.getTimeZone("UTC"));		        
        // For the English Locale, the timeZone MUST be GMT
        if (locale.equals(Locale.ENGLISH)) {
            // Change the timezone to GMT
            TimeZone zone = formatter.getTimeZone();
            final int msInMin = 60000;
            final int minInHr = 60;
            int minutes = zone.getOffset( theDate.getTime() ) / msInMin;
            int hours = minutes / minInHr; 
            zone = TimeZone.getTimeZone( "GMT Time" + (hours >= 0 ? "+" : "") + hours + ":" + minutes);
            formatter.setTimeZone( zone );
        }
        return formatter.format(theDate);
    }
    /**
     * Gets the date formated depending on the language
     * @param date 
     * @param lang 
     * @param langFormats map with the language-dependent date formats
     * @return 
     */
    public static String format(final Date date,final Language lang,
    							final Map<Language,String> langFormats) {
        String fmt = Dates.langFormat(lang,langFormats);
        return Dates.format(date,fmt);
    }
///////////////////////////////////////////////////////////////////////////////////////////
// 	FORMAT METHODS
///////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Gets a date from it's ISO representation
     * @param dateStr
     * @return
     */
    public static Date fromISO8601FormattedString(final String dateStr) {
    	return Dates.fromFormatedString(dateStr,"iso");
    }
    public static Date fromUTC(final String dateStr) {
    	return Dates.fromFormatedString(dateStr,"iso");
    }
    /**
     * Gets a date from it's string representation
     * It returns null if the provided String representation cannot be parsed to a Date
     * If the format parameter is "milis" it assumes that the date string is in milliseconds 
     * If the format parameter is "epoch" it assumes that the date string is in epoch timestamp format
     * If the format parameter is "seconds" it assumes that the date string is in seconds 
     * If the format parameter is "iso", "iso8601" or "utc" it assumes that the date string is in iso/utc format
     * @param dateStr 
     * @param format 
     * @return 
     */
	@SuppressWarnings("null")
	public static Date fromFormatedString(final String dateStr,final String format) {
        if (dateStr == null) return null;
    	String theDateStr = new String(dateStr);
     
    	String fmt = (format == null ) ? null 
    								   : new String(format);
    	if (Strings.isNullOrEmpty(fmt)) fmt = DEFAULT_FORMAT;			
    	if (Strings.isNullOrEmpty(theDateStr)) return new Date();	
    	
    	boolean isISO = fmt.equalsIgnoreCase("iso") 
    			     || fmt.equalsIgnoreCase("utc") 
    			     || fmt.equalsIgnoreCase("iso8601")
    			     || fmt.equalsIgnoreCase(Dates.ISO8601);
   	
        if ((fmt.equalsIgnoreCase("millis") || fmt.equalsIgnoreCase("milis")) && Numbers.isLong(dateStr)) {		// bug with millis WTF!
            return new Date( Long.parseLong(dateStr) );
        } else if (fmt.equalsIgnoreCase("seconds") && Numbers.isLong(dateStr)) {
            return new Date( Long.parseLong(dateStr)*1000L );
        } else if (fmt.equalsIgnoreCase("epoch")) {
        	fmt = Dates.EPOCH;		// "MMM dd yyyy HH:mm:ss.SSS zzz"
        } else if (isISO) {
        	fmt = Dates.ISO8601;
        }
        SimpleDateFormat formatter = new SimpleDateFormat(fmt);
        if (isISO) formatter.setTimeZone(TimeZone.getTimeZone("UTC"));		// BEWARE!! when the input string is in UTC format
        formatter.setLenient(false);    // strict format
        ParsePosition pos = new ParsePosition(0);
        Date outDate = formatter.parse(theDateStr,pos);        
        
        return outDate;
    }
    /**
     * Gests a date from it's string language-dependent representation
     * @param dateStr
     * @param lang
     * @param langFormats the language date formats
     * @return the parsed date of null if the date cannot be parsed
     */
    public static java.util.Date fromLanguageFormatedString(final String dateStr,final Language lang,
    														final Map<Language,String> langFormats) {
        if (dateStr == null) return null;
        String fmt = Dates.langFormat(lang,langFormats);
        return fromFormatedString(dateStr,fmt);
    }
///////////////////////////////////////////////////////////////////////////////////////////
//  OTHER METHODS
///////////////////////////////////////////////////////////////////////////////////////////   
    /**
     * Reformats a date as string to another format
     * @param dateStr the date in the source format
     * @param sourceFormat the source format
     * @param targetFormat the target format
     * @return 
     */
    public static String reformat(final String dateStr,
    							  final String sourceFormat,final String targetFormat) {
        String theOldFmt = Strings.isNullOrEmpty(sourceFormat) ? DEFAULT_FORMAT : sourceFormat; 
        String theNewFmt = Strings.isNullOrEmpty(targetFormat) ? DEFAULT_FORMAT : targetFormat;
        String theDateStr = Strings.isNullOrEmpty(dateStr) ? Dates.format(new Date(),theOldFmt) : dateStr;

        Date newDate = Dates.fromFormatedString(theDateStr,theOldFmt);
        return Dates.format(newDate,theNewFmt);
    }
///////////////////////////////////////////////////////////////////////////////////////////
//  FORMATEO DE FECHAS EN EUSKARA Y CASTELLANO
///////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Obtiene la fecha actual como una cadena
     * @param language El lenguaje 0=Castellano, 1=Euskara
     * @return La fecha actual como una cadena formateada seg�n el lenguaje
     */
    public static String currentDate(final Language language) {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+1:00"),new Locale("es","ES"));
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        int monthOfYear = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);

        String outDate = null;
        switch (language) {
            case SPANISH:
            	// Lunes, 25 de abril de 1995
                outDate = Strings.create()
                				 .add(_getDayOfWeekName(dayOfWeek,Language.SPANISH)).add(", ")
                				 .add(Integer.toString(calendar.get(Calendar.DAY_OF_MONTH))).add(" de ")
                				 .add(_getMonthName(monthOfYear,Language.SPANISH)).add(" de ")
                				 .add(Integer.toString(year)).asString();
                break;
            case BASQUE:
                outDate = Strings.create()
                				 .add(_getDayOfWeekName(dayOfWeek,Language.BASQUE)).add(", ")
                				 .add(Integer.toString(year)).add("-ko ")
                				 .add(_getMonthName(monthOfYear,Language.BASQUE)).add("ren ")
                				 .add(Integer.toString(calendar.get(Calendar.DAY_OF_MONTH))).asString();
                break;
            case ENGLISH:
            	// 2012 Monday, April the 1st
                throw new IllegalArgumentException("english language... not implemented!");
            case FRENCH:
            	throw new IllegalArgumentException("frech language... not implemented!");
            case DEUTCH:
            	throw new IllegalArgumentException("english language... not implemented!");
            case ANY:
            	throw new IllegalArgumentException("unknown language... not implemented!");
            default:
            	outDate = day + "/" + monthOfYear + "/" + year;
            
        }
        return outDate;
    }
    private static String _getDayOfWeekName(final int dayOfWeek,Language language) {
    	String outDayName = null;
        switch(language) {
            case SPANISH:
                outDayName = _getDayOfWeekInCastellano(dayOfWeek);
                break;
            case BASQUE:
                outDayName = _getDayOfWeekInEuskera(dayOfWeek);
                break;
            case ENGLISH:
            	// 2012 Monday, April the 1st
                throw new IllegalArgumentException("english language... not implemented!");
            case FRENCH:
            	throw new IllegalArgumentException("frech language... not implemented!");
            case DEUTCH:
            	throw new IllegalArgumentException("english language... not implemented!");
            case ANY:
            	throw new IllegalArgumentException("unknown language... not implemented!");
            default:
            	outDayName = "";
        }
        return outDayName;
    }
    private static String _getMonthName(final int month,final Language language) {
    	String outMonthName = null;
        switch(language) {
            case SPANISH:
                outMonthName = _getMonthNameInCastellano(month);
                break;
            case BASQUE:
                outMonthName = _getMonthNameInEuskera(month);
                break;
            case ENGLISH:
            	// 2012 Monday, April the 1st
                throw new IllegalArgumentException("english language... not implemented!");
            case FRENCH:
            	throw new IllegalArgumentException("frech language... not implemented!");
            case DEUTCH:
            	throw new IllegalArgumentException("english language... not implemented!");
            case ANY:
            	throw new IllegalArgumentException("unknown language... not implemented!");
            default:
            	outMonthName = "";
        }
        return outMonthName;
    }
    private static String _getDayOfWeekInEuskera(final int dayOfWeek) {
    	String outDayOfWeek = null;
        switch(dayOfWeek) {
            case Calendar.SUNDAY:   outDayOfWeek = "Igandea";	break;
            case Calendar.MONDAY:   outDayOfWeek = "Astelehena";break;
            case Calendar.TUESDAY:  outDayOfWeek = "Asteartea";	break;
            case Calendar.WEDNESDAY:outDayOfWeek = "Asteazkena";break;
            case Calendar.THURSDAY: outDayOfWeek = "Osteguna";	break;
            case Calendar.FRIDAY:   outDayOfWeek = "Ostirala";	break;
            case Calendar.SATURDAY: outDayOfWeek = "Larunbata";	break;
            default:				outDayOfWeek = "";
        }
        return outDayOfWeek;
    }
    private static String _getDayOfWeekInCastellano(final int dayOfWeek) {
    	String outDayOfWeek = null;
        switch(dayOfWeek) {
            case Calendar.SUNDAY:   outDayOfWeek = "Domingo";	break;
            case Calendar.MONDAY:   outDayOfWeek = "Lunes";		break;
            case Calendar.TUESDAY:  outDayOfWeek = "Martes";	break;	
            case Calendar.WEDNESDAY:outDayOfWeek = "Mi�rcoles";	break;
            case Calendar.THURSDAY: outDayOfWeek = "Jueves";	break;
            case Calendar.FRIDAY:   outDayOfWeek = "Viernes";	break;	
            case Calendar.SATURDAY: outDayOfWeek = "S�bado";	break;
            default:				outDayOfWeek = "";
        }
        return outDayOfWeek;
    }
    private static String _getMonthNameInEuskera(final int month) {
    	String outMonthName = null;
        switch(month) {
            case Calendar.JANUARY:  outMonthName = "Urtarila"; break;
            case Calendar.FEBRUARY: outMonthName = "Otsaila";  break;
            case Calendar.MARCH:    outMonthName = "Martxoa";  break;
            case Calendar.APRIL:    outMonthName = "Aprila";   break;
            case Calendar.MAY:      outMonthName = "Maiatza";  break;
            case Calendar.JUNE:     outMonthName = "Ekaina";   break;
            case Calendar.JULY:     outMonthName = "Uztaila";  break;
            case Calendar.AUGUST:   outMonthName = "Abuztua";  break;
            case Calendar.SEPTEMBER:outMonthName = "Iraila";   break;
            case Calendar.OCTOBER:  outMonthName = "Urria";    break;
            case Calendar.NOVEMBER: outMonthName = "Azaroa";   break;
            case Calendar.DECEMBER: outMonthName = "Abendua";  break;
            default:				outMonthName = "";
        }
        return outMonthName;
    }
    private static String _getMonthNameInCastellano(final int month) {
    	String outMonthName = null;
        switch(month) {
            case Calendar.JANUARY:  outMonthName = "Enero";     break;
            case Calendar.FEBRUARY: outMonthName = "Febrero";   break;
            case Calendar.MARCH:    outMonthName = "Marzo";     break;
            case Calendar.APRIL:    outMonthName = "Abril";     break;
            case Calendar.MAY:      outMonthName = "Mayo";      break;
            case Calendar.JUNE:     outMonthName = "Junio";     break;
            case Calendar.JULY:     outMonthName = "Julio";     break;
            case Calendar.AUGUST:   outMonthName = "Agosto";    break;
            case Calendar.SEPTEMBER:outMonthName = "Septiembre";break;
            case Calendar.OCTOBER:  outMonthName = "Octubre";   break;
            case Calendar.NOVEMBER: outMonthName = "Noviembre"; break;
            case Calendar.DECEMBER: outMonthName = "Diciembre"; break;
            default:				outMonthName = "";
        }
        return outMonthName;
    }
///////////////////////////////////////////////////////////////////////////////////////////
//  OTROS METODOS
///////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Pasa una fecha al maximo, es decir si la fecha suministrada es 25/03/07 11:44:00 pasa
     * a 25/03/07 23:59:999
     * @param date la fecha
     * @return otra fecha en el ultimo mili
     */
    public static Date rollDateToMaximum(final Date date) {
        Calendar theCal = Calendar.getInstance(TimeZone.getTimeZone("GMT+1:00"),new Locale("es","ES"));
        theCal.setTime(date);
        return Dates.rollCalendarToMaximum(theCal).getTime();
    }
    /**
     * Pasa una fecha al minimo, es decir si la fecha suministrada es 25/03/07 11:44:00 pasa
     * a 25/03/07 00:00:000
     * @param date la fecha
     * @return otra fecha en el primer mili
     */
    public static Date rollDateToMinimum(final Date date) {
        Calendar theCal = Calendar.getInstance();
        theCal.setTime(date);
        return Dates.rollCalendarToMinimum(theCal).getTime();
    }
    /**
     * Pasa una fecha al maximo, es decir si la fecha suministrada es 25/03/07 11:44:00 pasa
     * a 25/03/07 23:59:999
     * @param theCal la fecha
     * @return otra fecha en el ultimo mili
     */
    public static Calendar rollCalendarToMaximum(final Calendar theCal) {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+1:00"),new Locale("es","ES"));
        cal.setTime(theCal.getTime());
        cal.set(Calendar.HOUR_OF_DAY,cal.getActualMaximum(Calendar.HOUR_OF_DAY));
        cal.set(Calendar.MINUTE,cal.getActualMaximum(Calendar.MINUTE));
        cal.set(Calendar.MILLISECOND,cal.getActualMaximum(Calendar.MILLISECOND));
        return cal;
    }
    /**
     * Pasa una fecha al minimo, es decir si la fecha suministrada es 25/03/07 11:44:00 pasa
     * a 25/03/07 00:00:000
     * @param theCal la fecha
     * @return otra fecha en el primer mili
     */
    public static Calendar rollCalendarToMinimum(final Calendar theCal) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(theCal.getTime());
        cal.set(Calendar.HOUR_OF_DAY,cal.getActualMinimum(Calendar.HOUR_OF_DAY));
        cal.set(Calendar.MINUTE,cal.getActualMinimum(Calendar.MINUTE));
        cal.set(Calendar.MILLISECOND,cal.getActualMinimum(Calendar.MILLISECOND));
        return cal;
    }
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Returns a Joda-Time's {@link Interval} object with a date day start and day end
	 * @param date
	 * @return
	 */
	public static Interval dateDayStartAndEndIntervalFor(final Date date) {
		DateTime dateTime = new DateTime(date,
										 DateTimeZone.getDefault());
		DateTime dayStart = dateTime.withTimeAtStartOfDay();		// start of the day	
		DateTime dayAfterStart = dateTime.plusDays(1)				// 
										 .withTimeAtStartOfDay();	// start of the date after
		Interval dateInterval = new Interval( dayStart, dayAfterStart );
		return dateInterval;
	}
}
