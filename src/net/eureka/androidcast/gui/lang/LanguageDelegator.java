package net.eureka.androidcast.gui.lang;

/**
 * Handles the retrieval of strings in different languages, namely English. When a language specific
 * String is required; The static method getLanguageOfComponent will help by detecting the locale of the O.S then
 * return the appropriate string from {@link Languages}.
 *  
 * @author Owen McMonagle.
 * @see Detect
 * @see Languages
 */
public final class LanguageDelegator 
{
	
	/**
	 * Retrieves a language specific string based upon the systems locale being English(en).
	 * @param {@link Languages} language_component - The enumeration of the language specific string needed. E.g ERROR.
	 * @return String - Language specific string from {@link Languages}.
	 */
	public static String getLanguageOfComponent(Languages language_component)
	{	
		// Return the English string.  
		return language_component.getEnglish();
	}
}
