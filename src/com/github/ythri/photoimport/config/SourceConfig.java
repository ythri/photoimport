package com.github.ythri.photoimport.config;

public class SourceConfig {
	public enum SearchMode { recursive, single, dcf }

	public String path;
	public SearchMode searchMode = SearchMode.recursive;
	public boolean groups = true;
}
