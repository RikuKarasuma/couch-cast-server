package net.eureka.couchcast.gui.lang;


import net.eureka.couchcast.Static;

/**  ** FOR FUTURE LANG SUPPORT.
 * Detects whether system language is English. Used by the {@link LanguageDelegator} to detect windows locale.
 * Once a language has been detected, the {@link LanguageDelegator} can then choose an appropriate {@link String} from
 * {@link Languages}.
 * 
 * @author Owen McMonagle.
 * @see LanguageDelegator
 * @see Languages
 */
public final class Detect 
{
	/**
	 * Identifier used to find if the system locale is English.
	 */
	private static final String ENGLISH_LOCALE_KEY = "en";
	
	/**
	 * Flag which indicates which language the O.S is using. It determines this by retrieving the system locale from
	 * {@link Static}. If returns true, then the language is English.
	 */
	private static final boolean IS_LANGUAGE_ENGLISH = ((Static.getSystemLocale().equals(ENGLISH_LOCALE_KEY)) ? true : false);
	 
	/**
	 * Retrieves whether the language is English or not.
	 * @return
	 */
	public static boolean isLanguageEnglish()
	{
		return IS_LANGUAGE_ENGLISH;
	}
}
