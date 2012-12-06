package com.ami.gui2go;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class UserGuideActivity extends Activity
{
	ListView topicsList;
	WebView helpWebView;
	ProgressDialog progressBar;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_guide_activity);

		topicsList = (ListView) findViewById(R.id.topicsList);
		topicsList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		helpWebView = (WebView) findViewById(R.id.helpWebView);

		helpWebView.getSettings().setJavaScriptEnabled(true);
		helpWebView.setScrollBarStyle(WebView.SCROLLBARS_INSIDE_OVERLAY);

		helpWebView.setWebViewClient(new WebViewClient()
		{
			public boolean shouldOverrideUrlLoading(WebView view, String url)
			{
				view.loadUrl(url);
				return true;
			}

			public void onPageFinished(WebView view, String url)
			{
				if (progressBar.isShowing()) {
					progressBar.dismiss();
				}
			}

			public void onReceivedError(WebView view, int errorCode,
					String description, String failingUrl)
			{
				Toast.makeText(UserGuideActivity.this,
						"Error loading page: " + description,
						Toast.LENGTH_SHORT).show();
			}
		});

		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, R.array.helpTopics, android.R.layout.simple_list_item_activated_1);
		topicsList.setAdapter(adapter);

		topicsList.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id)
			{
				((ListView) parent).setItemChecked(position, true);
				
				String urlToLoad = new String();
				switch(position){
					case 0:
						urlToLoad = "http://gui2go.blogspot.com/p/gui2go-help.html";
						break;
					case 1:
						urlToLoad = "http://gui2go.blogspot.com/p/gui-2-go-help-activity-management.html";
						break;
					case 2:
						urlToLoad = "http://gui2go.blogspot.com/p/placing-and-editing-widgets.html";
						break;
					case 3:
						urlToLoad = "http://gui2go.blogspot.com/p/gui-2-go-help-exporting-your-files.html";
						break;
					
				}
				progressBar = ProgressDialog.show(UserGuideActivity.this,
						"Loading help page...", "Loading...",true,true,new OnCancelListener()
						{
							@Override
							public void onCancel(DialogInterface dialog)
							{
								helpWebView.stopLoading();
							}
						});
				helpWebView.loadUrl(urlToLoad);
			}
		});
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				return (true);
		}
		return super.onOptionsItemSelected(item);
	}
}
