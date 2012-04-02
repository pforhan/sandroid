package sans.newsreader.mvc;

import sans.newsreader.core.NewsArticle;
import sans.newsreader.core.NewsSource;

/** SANDROID note no android classes in use. */
public class DefaultArticleController implements ArticleController {
  private ArticleDisplay display;

  @Override public void setDisplay(ArticleDisplay display) {
    this.display = display;
  }

  @Override public void onCreate(boolean hasTwoPanes, int categoryIndex, int articleIndex) {
    // If we are in two-pane layout mode, this activity is no longer necessary
    if (hasTwoPanes) {
      display.finish();
      return;
    }

    // Display the correct news article.
    NewsArticle article = NewsSource.getInstance().getCategory(categoryIndex)
        .getArticle(articleIndex);
    display.displayArticle(article);
  }
}
