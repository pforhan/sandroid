package sans.newsreader.mvc;

import sans.newsreader.core.NewsArticle;
import sans.newsreader.core.NewsCategory;


/** The framework implementation of an NewsReader display. */
public interface NewsreaderDisplay {

  void setCategory(String title, NewsCategory category);
  void setUpActionBar(String[] categories, boolean hasTwoPanes, int categoryIndex);
  void setArticle(NewsArticle article);
  void showArticleActivity(int categoryIndex, int articleIndex);
  void showCategoryDialog(String[] categories);
}
