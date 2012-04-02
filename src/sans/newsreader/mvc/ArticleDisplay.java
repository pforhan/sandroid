package sans.newsreader.mvc;

import sans.newsreader.core.NewsArticle;

/** The framework implementation of an article display. */
public interface ArticleDisplay {
  void finish();
  void displayArticle(NewsArticle article);
}
