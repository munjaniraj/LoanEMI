package com.loanemi.calculator.emi.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import com.loanemi.calculator.emi.R;
import com.facebook.shimmer.BuildConfig;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.loanemi.calculator.emi.Ads.NativeAdPreloader;
import com.loanemi.calculator.emi.Activities.LegalPageActivity;
import com.loanemi.calculator.emi.MainActivity;
import com.loanemi.calculator.emi.helper.PlayStoreHelper;
import com.loanemi.calculator.emi.helper.RateUsDialogHelper;
import com.loanemi.calculator.emi.language.LanguageActivity;
import com.loanemi.calculator.emi.language.LocaleHelper;
import com.loanemi.calculator.emi.utils.AppPreference;
import com.loanemi.calculator.emi.utils.MyApplication;
import com.loanemi.calculator.emi.utils.UiSafe;
import com.loanemi.calculator.emi.utils.Util;

public class SettingFragment extends Fragment {

    CardView cardLanguage, cardPrivacy, cardTerms, cardShareApp, cardRate, cardTheme;
    TextView txtVersion;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(LocaleHelper.setLocale(context));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_setting, container, false);

        initViews(view);
        setupAds(view);
        setupClickListeners();
        setupBackPressed();
        return view;
    }



    @SuppressLint("SetTextI18n")
    private void initViews(View view) {
        cardLanguage = view.findViewById(R.id.cardLanguage);
        cardTheme = view.findViewById(R.id.cardTheme);
        cardPrivacy = view.findViewById(R.id.cardPrivacy);
        cardTerms = view.findViewById(R.id.cardTerms);
        cardShareApp = view.findViewById(R.id.cardShareApp);
        cardRate = view.findViewById(R.id.cardRate);
        txtVersion = view.findViewById(R.id.txtVersion);

        txtVersion.setText(": " + BuildConfig.VERSION_NAME);
    }

    private void setupClickListeners() {
        cardLanguage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(requireContext(), LanguageActivity.class);
                intent.putExtra(
                        AppPreference.EXTRA_LANG_OPENED_IN_SETTINGS,
                        true
                );
                startActivity(intent);
            }
        });
        cardPrivacy.setOnClickListener(v ->
                startActivity(LegalPageActivity.createIntent(requireContext(), LegalPageActivity.TYPE_PRIVACY)));
        cardTerms.setOnClickListener(v ->
                startActivity(LegalPageActivity.createIntent(requireContext(), LegalPageActivity.TYPE_TERMS)));
        cardTheme.setOnClickListener(v -> showThemeDialog());
        cardShareApp.setOnClickListener(v -> shareApp(requireContext()));
        cardRate.setOnClickListener(v -> showRateFromMenu());
    }

    private void showThemeDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog_theme, null);

        RadioButton radioDefault = view.findViewById(R.id.radioDefault);
        RadioButton radioDark = view.findViewById(R.id.radioDark);
        RadioButton radioLight = view.findViewById(R.id.radioLight);
        AppCompatButton btnSave = view.findViewById(R.id.btnSaveTheme);

        String currentTheme = AppPreference.getInstance(requireContext()).getString(AppPreference.KEY_THEME, AppPreference.DEFAULT);


        if ("dark".equals(currentTheme)) {
            radioDark.setChecked(true);
        } else if ("light".equals(currentTheme)) {
            radioLight.setChecked(true);
        } else {
            radioDefault.setChecked(true);
        }


        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setView(view)
                .create();

        btnSave.setOnClickListener(v -> {
            String theme;


            if (radioDark.isChecked()) {
                theme = "dark";
            } else if (radioLight.isChecked()) {
                theme = "light";
            } else {
                theme = AppPreference.DEFAULT;
            }

            AppPreference.getInstance(requireContext()).setString(AppPreference.KEY_THEME, theme);

            dialog.dismiss();
            MyApplication.applyTheme();
        });

        dialog.show();

        Window window = dialog.getWindow();

        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);

            DisplayMetrics metrics = getResources().getDisplayMetrics();

            int width = (int) (metrics.widthPixels * 0.85);

            window.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);
        }

        Util.hideUiWithDialog(dialog);
    }

    public void shareApp(Context context) {
        String packageName =context.getPackageName();
        String shareText = getString(R.string.share_text) + packageName;

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        context.startActivity(Intent.createChooser(shareIntent, "Share App via"));
    }

    public void showRateFromMenu() {
        if (!UiSafe.isAlive(this)) {
            return;
        }

        RateUsDialogHelper.show((AppCompatActivity) requireActivity(), new RateUsDialogHelper.Listener() {
            @Override
            public void onLowRating(int rating) {
                showThankYouDialog();
            }

            @Override
            public void onHighRating(int rating) {
                markUserRated();
                PlayStoreHelper.openAppPage(requireContext());
            }
        });
    }

    private void showThankYouDialog() {

        new AlertDialog.Builder(requireContext())
                .setTitle("Thank You")
                .setMessage("Thank you for your feedback!")
                .setPositiveButton("OK", (dialog, which) -> {

                    dialog.dismiss();
                })
                .show();
    }

    private void markUserRated() {
        AppPreference.getInstance(requireContext()).setBoolean(AppPreference.KEY_USER_RATING, true);
    }

    private void setupAds(@NonNull View view) {
        if (Util.isInternetAvailable(this.requireActivity())) {
            FrameLayout nativeLayout = view.findViewById(R.id.setting_native_layout);
            ShimmerFrameLayout shimmerNative = view.findViewById(R.id.nativeShimmerLayout);

            NativeAdPreloader.show(
                    requireActivity(),
                    nativeLayout,
                    getString(R.string.setting_native_medium),
                    shimmerNative
            );
        }
    }

    private void setupBackPressed() {

        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {

                    @Override
                    public void handleOnBackPressed() {

                        requireActivity()
                                .getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.fragmentContainer, new HomeFragment())
                                .commit();

                        if (requireActivity() instanceof MainActivity) {
                            ((MainActivity) requireActivity()).selectHomeTab();
                        }
                    }
                }
        );
    }
}
