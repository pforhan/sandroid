package sans.newsreader.mvc;

/** Controller interface for the ArticleActivity. */
public interface ArticleController {
  void setDisplay(ArticleDisplay display);
  void onCreate(boolean hasTwoPanes, int categoryIndex, int articleIndex);
}
