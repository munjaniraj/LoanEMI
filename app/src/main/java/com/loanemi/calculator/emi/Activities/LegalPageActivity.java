package com.loanemi.calculator.emi.Activities;

import static com.loanemi.calculator.emi.utils.Util.setupEdgeToEdge;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.loanemi.calculator.emi.R;
import com.loanemi.calculator.emi.language.LocaleHelper;

public class LegalPageActivity extends AppCompatActivity {

    public static final String EXTRA_PAGE_TYPE = "extra_page_type";
    public static final String TYPE_PRIVACY = "privacy";
    public static final String TYPE_TERMS = "terms";

    public static Intent createIntent(Context context, String pageType) {
        Intent intent = new Intent(context, LegalPageActivity.class);
        intent.putExtra(EXTRA_PAGE_TYPE, pageType);
        return intent;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_legal_page);
        setupEdgeToEdge(this, R.id.main);

        ImageView backButton = findViewById(R.id.backButton);
        TextView tvTitle = findViewById(R.id.tvTitle);
        TextView tvHeadline = findViewById(R.id.tvHeadline);
        TextView tvBody = findViewById(R.id.tvBody);
        ImageView ivIcon = findViewById(R.id.ivIcon);
        AppCompatButton btnOpenLink = findViewById(R.id.btnOpenLink);

        String pageType = getIntent().getStringExtra(EXTRA_PAGE_TYPE);
        boolean isTerms = TYPE_TERMS.equals(pageType);

        @StringRes int titleRes = isTerms ? R.string.terms_conditions : R.string.privacy_policy;
        @StringRes int headlineRes = isTerms ? R.string.terms_page_headline : R.string.privacy_page_headline;
        @StringRes int bodyRes = isTerms ? R.string.terms_page_body : R.string.privacy_page_body;
        @StringRes int buttonRes = isTerms ? R.string.view_full_terms : R.string.view_full_privacy;
        @StringRes int urlRes = isTerms ? R.string.terms_conditions_url : R.string.privacy_policy_url;
        @DrawableRes int iconRes = isTerms ? R.drawable.ic_terms : R.drawable.privacy_ic;

        tvTitle.setText(titleRes);
        tvHeadline.setText(headlineRes);
        tvBody.setText(bodyRes);
        btnOpenLink.setText(buttonRes);
        ivIcon.setImageResource(iconRes);

        String url = getString(urlRes);

        backButton.setOnClickListener(v -> finish());
        btnOpenLink.setOnClickListener(v -> openWebUrl(url));
    }

    private void openWebUrl(String url) {
        try {
            if (url == null || url.trim().isEmpty()) {
                Toast.makeText(this, R.string.we_maintain_our_policy, Toast.LENGTH_SHORT).show();
                return;
            }
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (Exception e) {
            Toast.makeText(this, R.string.we_maintain_our_policy, Toast.LENGTH_SHORT).show();
        }
    }
}
