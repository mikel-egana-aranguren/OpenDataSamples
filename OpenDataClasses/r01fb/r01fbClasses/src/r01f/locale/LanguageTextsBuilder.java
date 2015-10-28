package r01f.locale;

import java.util.Collection;
import java.util.Map;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import r01f.bundles.ResourceBundleControl;
import r01f.locale.LanguageTexts.LangTextNotFoundBehabior;
import r01f.patterns.IsBuilder;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;


/**
 * Factory of {@link LanguageTexts} types
 * Usage:
 * <h2>If a {@link LanguageTexts} backed by a Map is to be created:</h2>
 * <pre class='brush:java'>
 *	LanguageTexts text = TextByLanguageBuilder.createMapBacked()
 *											  .withMissingLangTextBehavior(LangTextNotFoundBehabior.RETURN_NULL)
 *									  		  .addForLang(Language.BASQUE,"testu1")
 *									  		  .addForLang(Language.ENGLISH,"text1")
 *											  .finish();
 *	String text_in_spanish = text.getFor(Language.SPANISH);
 * </pre>
 * 
 * <h2>If a {@link LanguageTexts} backed by a I18N ResourceBundle is to be created:</h2>
 * <pre class='brush:java'>
 *	LanguageTexts text = TextByLanguageBuilder.createI18NBundleBacked()
 *											  .forBundle("myBundle")
 *											  .loadedAsDefinedAt(xmlProperties,
 *															     AppCode.forId("r01fb"),AppComponent.forId("test"),Path.of("/resourcesLoader[@id='myResourcesLoader']"))
 *											  .forKey("myMessageKey")
 *											  .withMissingLangTextBehavior(LangTextNotFoundBehabior.RETURN_NULL);
 *	String text_in_spanish = text.getFor(Language.SPANISH);
 * </pre>
 */
public class LanguageTextsBuilder 
  implements IsBuilder {
/////////////////////////////////////////////////////////////////////////////////////////
//  MapBacked Builder
/////////////////////////////////////////////////////////////////////////////////////////
	public static LanguageTextsMapBackedBuilderBehaviorStep createMapBacked() {
		return new LanguageTextsMapBackedBuilderBehaviorStep(new LanguageTextsMapBacked());
	}
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public static class LanguageTextsMapBackedBuilderBehaviorStep {		
		private final LanguageTextsMapBacked _langTexts;
		
		/**
		 * Sets the behavior when a searched text is not found
		 * @param behabior return null / throw an exception / return a default text
		 */
		public LanguageTextsMapBackedBuilderAddStep withMissingLangTextBehavior(final LangTextNotFoundBehabior behabior) {
			_langTexts.setLangTextNotFoundBehabior(behabior);
			return new LanguageTextsMapBackedBuilderAddStep(_langTexts);
		}
		/**
		 * Sets the default text to return when a searched text in a language is not found
		 * @param defaultValue the the default value (it can contain a var values as {} that is going to be replaced by the language)
		 */
		public LanguageTextsMapBackedBuilderAddStep returningWhenLangTextMissing(final String defaultValue) {
			if (_langTexts.getLangTextNotFoundBehabior() != LangTextNotFoundBehabior.RETURN_DEFAULT_VALUE) {
				// ensure the LangTextNotFoundBehabior is RETURN_DEFAULT_VALUE
				_langTexts.setLangTextNotFoundBehabior(LangTextNotFoundBehabior.RETURN_DEFAULT_VALUE);
			}
			_langTexts.setDefaultValue(defaultValue);
			return new LanguageTextsMapBackedBuilderAddStep(_langTexts);
		}
	}
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public static class LanguageTextsMapBackedBuilderAddStep {
		private final LanguageTextsMapBacked _langTexts;
		
		/**
		 * Adds a text for a language
		 * @param lang the lang
		 * @param text the text
		 */
		public LanguageTextsMapBackedBuilderAddStep addForLang(final Language lang,final String text) {
			if (!Strings.isNullOrEmpty(text)) _langTexts.add(lang,text);
			return this;
		}
		/**
		 * Adds the same text to all the lang provided
		 * @param text the text
		 * @param langs the lang
		 */
		public LanguageTextsMapBackedBuilderAddStep addForLangs(final String text,final Language... langs) {
			if (CollectionUtils.hasData(langs)) {
				for (Language lang : langs) {
					this.addForLang(lang,text);
				}
			}
			return this;
		}
		/**
		 * Adds all the texts by lang provided in a {@link Map}
		 * @param texts
		 * @return
		 */
		public LanguageTextsMapBackedBuilderAddStep addAll(final Map<Language,String> texts) {
			if (CollectionUtils.hasData(texts)) {
				for (Map.Entry<Language,String> me : texts.entrySet()) {
					this.addForLang(me.getKey(),me.getValue());
				}
			}
			return this;
		}
		/**
		 * Adds the same text for ALL the languages
		 * @param text
		 * @return
		 */
		public LanguageTextsMapBackedBuilderAddStep addForAll(final String text) {
			for (Language lang : Language.values()) {
				this.addForLang(lang,text);
			}
			return this;
		}
		public LanguageTextsMapBacked build() {
			return _langTexts;
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
// 	I18NBundleBacked Builder
/////////////////////////////////////////////////////////////////////////////////////////
	public static LanguageTextsI18NBundleBackedBuilderControlStep createI18NBundleBacked() {
		return new LanguageTextsI18NBundleBackedBuilderControlStep();
	}
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public static class LanguageTextsI18NBundleBackedBuilderControlStep {	
		
		@SuppressWarnings("static-method")
		public LanguageTextsI18NBundleBackedBuilderBundleStep loadedAsDefinedAt(final ResourceBundleControl resBundleControl) {
			return new LanguageTextsI18NBundleBackedBuilderBundleStep(new LanguageTextsI18NBundleBacked(resBundleControl));
		}
	}
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public static class LanguageTextsI18NBundleBackedBuilderBundleStep {
		private final LanguageTextsI18NBundleBacked _langTexts;
		
		/**
		 * Sets the bundle chain: the bundles that'll be searched in order to find the key
		 * @param bundleChain
		 */
		public LanguageTextsI18NBundleBackedBackedBuilderBehaviorStep forBundle(final String... bundleChain) {
			if (CollectionUtils.hasData(bundleChain)) {
				if (bundleChain.length == 1) {
					_langTexts.setBundleChain(bundleChain[0].split(","));
				} else {
					_langTexts.setBundleChain(bundleChain);
				}
			}
			return new LanguageTextsI18NBundleBackedBackedBuilderBehaviorStep(_langTexts);
		}
		/**
		 * Sets the bundle chain: the bundles that'll be searched in order to find the key
		 * @param bundleChain
		 */
		public LanguageTextsI18NBundleBackedBackedBuilderBehaviorStep forBundle(final Collection<String> bundleChain) {
			return this.forBundle(bundleChain.toArray(new String[bundleChain.size()]));
		}
	}
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public static class LanguageTextsI18NBundleBackedBackedBuilderBehaviorStep {		
		private final LanguageTextsI18NBundleBacked _langTexts;
		
		/**
		 * Sets the behavior when a searched text is not found
		 * @param behabior return null / throw an exception / return a default text
		 */
		public LanguageTextsI18NBundleBacked withMissingLangTextBehavior(final LangTextNotFoundBehabior behabior) {
			_langTexts.setLangTextNotFoundBehabior(behabior);
			return _langTexts;
		}
		/**
		 * Sets the default text to return when a searched text in a language is not found
		 * @param defaultValue the the default value (it can contain a var values as {} that is going to be replaced by the language)
		 */
		public LanguageTextsI18NBundleBacked returningWhenLangTextMissing(final String defaultValue) {
			if (_langTexts.getLangTextNotFoundBehabior() != LangTextNotFoundBehabior.RETURN_DEFAULT_VALUE) {
				// ensure the LangTextNotFoundBehabior is RETURN_DEFAULT_VALUE
				_langTexts.setLangTextNotFoundBehabior(LangTextNotFoundBehabior.RETURN_DEFAULT_VALUE);
			}
			_langTexts.setDefaultValue(defaultValue);
			return _langTexts;
		}
	}

}
