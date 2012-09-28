package com.stiggpwnz.vibes;

import android.content.SearchRecentSuggestionsProvider;

public class SearchProvider extends SearchRecentSuggestionsProvider {

	public static final String AUTHORITY = "Vibes";
	public static final int MODE = DATABASE_MODE_QUERIES;

	public SearchProvider() {
		super();
		setupSuggestions(AUTHORITY, MODE);
	}

}
