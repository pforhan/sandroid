/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package sans.newsreader.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SpinnerAdapter;
import com.example.android.newsreader.R;
import sans.newsreader.core.NewsArticle;
import sans.newsreader.core.NewsCategory;
import sans.newsreader.mvc.DefaultNewsreaderController;
import sans.newsreader.mvc.NewsreaderController;
import sans.newsreader.mvc.NewsreaderDisplay;

/**
 * Main activity: shows headlines list and articles, if layout permits.
 *
 * This is the main activity of the application. It can have several different layouts depending on
 * the SDK version, screen size and orientation. The configurations are divided in two large groups:
 * single-pane layouts and dual-pane layouts.
 *
 * In single-pane mode, this activity shows a list of headlines using a {@link sans.newsreader.ui.HeadlinesFragment}.
 * When the user clicks on a headline, a separate activity (a {@link sans.newsreader.ui.ArticleActivity}) is launched
 * to show the news article.
 *
 * In dual-pane mode, this activity shows a {@HeadlinesFragment} on the left
 * side and an {@ArticleFragment} on the right side. When the user selects a
 * headline on the left, the corresponding article is shown on the right.
 *
 * If an Action Bar is available (large enough screen and SDK version 11 or up), navigation controls
 * are shown in the Action Bar (whether to show tabs or a list depends on the layout). If an Action
 * Bar is not available, a regular image and button are shown in the top area of the screen,
 * emulating an Action Bar.
 *
 * SANDROID note how there is very little logic and very few member variables contained herein.
 */
public class NewsReaderActivity extends FragmentActivity implements
    HeadlinesFragment.OnHeadlineSelectedListener, CompatActionBarNavListener, OnClickListener,
    NewsreaderDisplay {

  // TODO SANDROID Normally, this would be guice or otherwise injected.
  private NewsreaderController controller = new DefaultNewsreaderController();

  // The fragment where the headlines are displayed
  HeadlinesFragment mHeadlinesFragment;

  // The fragment where the article is displayed (null if absent)
  ArticleFragment mArticleFragment;

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main_layout);
    // SANDROID This should be done immediately upon onCreate (or maybe in onResume?), especially
    // in cases where a dependency framework is reusing controllers.
    controller.setDisplay(this);

    // find our fragments
    mHeadlinesFragment = (HeadlinesFragment) getSupportFragmentManager().findFragmentById(
        R.id.headlines);
    mArticleFragment = (ArticleFragment) getSupportFragmentManager().findFragmentById(R.id.article);

    // Determine whether we are in single-pane or dual-pane mode by testing the visibility
    // of the article view.
    View articleView = findViewById(R.id.article);
    boolean isDualPane = false;
    isDualPane = articleView != null && articleView.getVisibility() == View.VISIBLE;

    // Register ourselves as the listener for the headlines fragment events.
    // SANDROID we probably could just let the controller implement OHSL and set it in here...
    mHeadlinesFragment.setOnHeadlineSelectedListener(this);

    // Set up the Action Bar (or not, if one is not available)
    int catIndex = savedInstanceState == null ? 0 : savedInstanceState.getInt("catIndex", 0);
    controller.onCreate(isDualPane, catIndex);

    // Set up headlines fragment
    mHeadlinesFragment.setSelectable(isDualPane);
    restoreSelection(savedInstanceState);

    // Set up the category button (shown if an Action Bar is not available)
    Button catButton = (Button) findViewById(R.id.categorybutton);
    if (catButton != null) {
      catButton.setOnClickListener(this);
    }
  }

  /** Restore category/article selection from saved state. */
  void restoreSelection(Bundle savedInstanceState) {
    if (savedInstanceState != null) {
      int categoryIndex = savedInstanceState.getInt("catIndex", 0);
      int articleIndex = savedInstanceState.getInt("artIndex", -1);
      controller.onRestore(categoryIndex, articleIndex);
    }
  }

  @Override public void onRestoreInstanceState(Bundle savedInstanceState) {
    restoreSelection(savedInstanceState);
  }

  /**
   * Sets up Action Bar (if present).
   *
   * @param showTabs
   *          whether to show tabs (if false, will show list).
   * @param selTab
   *          the selected tab or list item.
   */
  @Override public void setUpActionBar(String[] categories, boolean showTabs, int selTab) {
    if (Build.VERSION.SDK_INT < 11) {
      // No action bar for you!
      // But do not despair. In this case the layout includes a bar across the
      // top that looks and feels like an action bar, but is made up of regular views.
      return;
    }

    android.app.ActionBar actionBar = getActionBar();
    actionBar.setDisplayShowTitleEnabled(false);

    // Set up a CompatActionBarNavHandler to deliver us the Action Bar nagivation events
    CompatActionBarNavHandler handler = new CompatActionBarNavHandler(this);
    if (showTabs) {
      actionBar.setNavigationMode(android.app.ActionBar.NAVIGATION_MODE_TABS);
      int i;
      // SANDROID note the loop logic, but this is necessary to interact with Android APIs.
      for (i = 0; i < categories.length; i++) {
        actionBar.addTab(actionBar.newTab().setText(categories[i]).setTabListener(handler));
      }
      actionBar.setSelectedNavigationItem(selTab);
    } else {
      actionBar.setNavigationMode(android.app.ActionBar.NAVIGATION_MODE_LIST);
      SpinnerAdapter adap = new ArrayAdapter<String>(this, R.layout.actionbar_list_item, categories);
      actionBar.setListNavigationCallbacks(adap, handler);
    }

    // Show logo instead of icon+title.
    actionBar.setDisplayUseLogoEnabled(true);
  }

  @Override public void onStart() {
    super.onStart();
    controller.onStart();
  }

  /**
   * Sets the displayed news category.
   *
   * This causes the headlines fragment to be repopulated with the appropriate headlines.
   */
  @Override public void setCategory(String categoryTitle, NewsCategory category) {
    mHeadlinesFragment.loadCategory(category);

    // If we are displaying a "category" button (on the ActionBar-less UI), we have to update
    // its text to reflect the current category.
    Button catButton = (Button) findViewById(R.id.categorybutton);
    if (catButton != null) {
      catButton.setText(categoryTitle);
    }
  }

  /**
   * Called when a headline is selected.
   *
   * This is called by the HeadlinesFragment (via its listener interface) to notify us that a
   * headline was selected in the Action Bar. The way we react depends on whether we are in single
   * or dual-pane mode. In single-pane mode, we launch a new activity to display the selected
   * article; in dual-pane mode we simply display it on the article fragment.
   *
   * @param index
   *          the index of the selected headline.
   */
  @Override public void onHeadlineSelected(int index) {
    controller.onHeadlineSelected(index);
  }

  @Override public void setArticle(NewsArticle article) {
    // display it on the article fragment
    mArticleFragment.displayArticle(article);
  }

  @Override public void showArticleActivity(int categoryIndex, int articleIndex) {
    // use separate activity
    Intent i = new Intent(this, ArticleActivity.class);
    i.putExtra("catIndex", categoryIndex);
    i.putExtra("artIndex", articleIndex);
    startActivity(i);
  }

  /**
   * Called when a news category is selected.
   *
   * This is called by our CompatActionBarNavHandler in response to the user selecting a news
   * category in the Action Bar. We react by loading and displaying the headlines for that category.
   *
   * @param catIndex
   *          the index of the selected news category.
   */
  @Override public void onCategorySelected(int catIndex) {
    controller.onCategorySelected(catIndex);
  }

  /** Save instance state. Saves current category/article index. */
  @Override protected void onSaveInstanceState(Bundle outState) {
    outState.putInt("catIndex", controller.getCategoryIndex());
    outState.putInt("artIndex", controller.getArticleIndex());
    super.onSaveInstanceState(outState);
  }

  /**
   * Called when news category button is clicked.
   *
   * This is the button that we display on UIs that don't have an action bar. This button calls up a
   * list of news categories and switches to the given category.
   */
  @Override public void onClick(View v) {
    controller.categoryButtonClicked();
  }

  @Override public void showCategoryDialog(String[] categories) {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle("Select a Category");
    builder.setItems(categories, new DialogInterface.OnClickListener() {
      @Override public void onClick(DialogInterface dialog, int which) {
        controller.onCategorySelected(which);
      }
    });
    AlertDialog d = builder.create();
    d.show();
  }
}
