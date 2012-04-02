package sans.newsreader.mvc;

/** Controller interface for the Newsreader. */
public interface NewsreaderController {
  void setDisplay(NewsreaderDisplay display);
  void onCreate(boolean hasTwoPanes, int categoryIndex);
  void onRestore(int categoryIndex, int articleIndex);
  void onStart();
  void onHeadlineSelected(int articleIndex);
  void onCategorySelected(int catIndex);
  int getCategoryIndex();
  int getArticleIndex();
  void categoryButtonClicked();
}
