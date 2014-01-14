package imagemover.config;

public class Source {
	public enum SearchMode { recursive, single, dcf }

	public String path;
	public SearchMode type = SearchMode.recursive;
	public boolean groups = true;
}
