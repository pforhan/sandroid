package sans.newsreader.mvc;

import sans.newsreader.core.NewsCategory;
import sans.newsreader.core.NewsSource;

public class DefaultNewsreaderController implements NewsreaderController {
  private static final int NO_ARTICLE = -1;

  // List of category titles
  final String CATEGORIES[] = { "Top Stories", "Politics", "Economy", "Technology" };

  private NewsreaderDisplay display;
  // Whether or not we are in dual-pane mode
  private boolean hasTwoPanes;
  // The news category and article index currently being displayed
  private int categoryIndex;
  private int articleIndex;

  @Override public void setDisplay(NewsreaderDisplay display) {
    this.display = display;
  }

  @Override public void onCreate(boolean hasTwoPanes, int categoryIndex) {
    this.hasTwoPanes = hasTwoPanes;
    display.setUpActionBar(CATEGORIES, hasTwoPanes, categoryIndex);
  }

  @Override public void onRestore(int categoryIndex, int articleIndex) {
    setCategory(categoryIndex, articleIndex);
  }

  private void setCategory(int categoryIndex, int articleIndex) {
    this.categoryIndex = categoryIndex;
    this.articleIndex = articleIndex;
    NewsCategory category = getCurrentCategory();
    display.setCategory(CATEGORIES[categoryIndex], category);
    // If we are displaying the article on the right, we have to update that too
    if (hasTwoPanes) {
      if (articleIndex == NO_ARTICLE) {
        // Default to first article.
        display.setArticle(category.getArticle(0));
      } else {
        display.setArticle(category.getArticle(articleIndex));
      }
    }
  }

  private NewsCategory getCurrentCategory() {
    return NewsSource.getInstance().getCategory(categoryIndex);
  }

  @Override public void onStart() {
    // This might have been 0,0 originally.
    setCategory(categoryIndex, articleIndex);
  }

  @Override public void onHeadlineSelected(int articleIndex) {
    this.articleIndex = articleIndex;
    if (hasTwoPanes) {
      // display it on the article fragment
      display.setArticle(getCurrentCategory().getArticle(articleIndex));
    } else {
      display.showArticleActivity(categoryIndex, articleIndex);
    }
  }

  @Override public void onCategorySelected(int catIndex) {
    setCategory(catIndex, -1);
  }

  @Override public int getArticleIndex() {
    return articleIndex;
  }

  @Override public int getCategoryIndex() {
    return categoryIndex;
  }

  @Override public void categoryButtonClicked() {
    display.showCategoryDialog(CATEGORIES);
  }
}
