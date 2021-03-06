package com.xiaohu.pdfdemo.activity;

import android.os.Environment;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewAnimator;

import com.artifex.mupdfdemo.Annotation;
import com.artifex.mupdfdemo.Hit;
import com.artifex.mupdfdemo.MuPDFAlert;
import com.artifex.mupdfdemo.MuPDFCore;
import com.artifex.mupdfdemo.MuPDFPageAdapter;
import com.artifex.mupdfdemo.MuPDFReaderView;
import com.artifex.mupdfdemo.MuPDFReaderViewListener;
import com.artifex.mupdfdemo.MuPDFView;
import com.artifex.mupdfdemo.OutlineActivityData;
import com.artifex.mupdfdemo.OutlineItem;
import com.artifex.mupdfdemo.ReaderView;
import com.artifex.mupdfdemo.SavePdf;
import com.artifex.mupdfdemo.SearchTask;
import com.artifex.mupdfdemo.SearchTaskResult;
import com.artifex.mupdfdemo.widget.VDHDeepLayout;
import com.lonelypluto.pdflibrary.utils.SharedPreferencesUtil;
import com.xiaohu.pdfdemo.R;

import java.util.concurrent.Executor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class AllPDFActivity extends AppCompatActivity {
    private static final String TAG = AllPDFActivity.class.getSimpleName();
    private final int OUTLINE_REQUEST = 0;
    private String filePath = Environment.getExternalStorageDirectory() + "/pdf_t1.pdf"; // ????????????
    //    private String filePath = Environment.getExternalStorageDirectory() + "/t"; // ????????????
    private String fileName;// ?????????

    private AlertDialog.Builder mAlertBuilder;
    static private AlertDialog.Builder gAlertBuilder;
//?????????????????????????????????????????????????????????
    private MuPDFCore muPDFCore;// ??????mupdf.so??????

    private MuPDFReaderView muPDFReaderView;// ??????pdf???view

    private boolean mAlertsActive = false;
    private AsyncTask<Void, Void, MuPDFAlert> mAlertTask;
    private AlertDialog mAlertDialog;

    // tools
    private ViewAnimator mTopBarSwitcher;// ???????????????
    private ImageButton mLinkButton;// ?????????
    private ImageButton mOutlineButton;// ??????
    private ImageButton mSearchButton;// ??????
    private ImageButton mAnnotButton;// ??????
    // tools ?????????
    private EditText mSearchText;// ?????????????????????
    private ImageButton mSearchBack;// ?????????????????????
    private ImageButton mSearchFwd;// ?????????????????????
    // tools ????????????
    private TextView mAnnotTypeText;// ????????????
    // tools ????????????
    private TextView mPageNumberView;// ??????
    private SeekBar mPageSlider;// ???????????????

    private int mPageSliderRes;// ??????????????????
    private boolean mButtonsVisible;// ?????????????????????
    private TopBarMode mTopBarMode = TopBarMode.Main;// ???????????????
    private AcceptMode mAcceptMode;// ?????????????????????

    private SearchTask mSearchTask;// ????????????
    private boolean mLinkHighlight = false;// ??????????????????


    private Button btn_change_hv;// ??????????????????
    private boolean ischangeHV = false;// ????????????
    private Button btn_linkhighlightcolor;// ?????????????????????
    private Button btn_searchtextcolor;// ????????????????????????
    private Button btn_paintcolor;// ??????????????????
    private Button btn_paintstrokewidth;// ??????????????????

    private Button btn_sign;// ????????????
    private Button btn_save;// ??????
    private VDHDeepLayout vdhDeepLayout;
    private ImageView iv_sign;

    private ImageView iv_test;

    private SavePdfTask savePdfTask;
    /*
     * ?????????????????????,???????????????
     * */
    class SavePdfTask extends AsyncTask {

        SavePdf savePdf;
        public SavePdfTask(SavePdf savePdf) {
            this.savePdf = savePdf;
        }

        @Override
        protected Object doInBackground(Object[] params) {
            savePdf.addText();
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            Log.e(TAG, "????????????");
            try {


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_pdf);

        initView();
    }

    private void initView() {
        SharedPreferencesUtil.init(getApplication());

        muPDFReaderView = (MuPDFReaderView)findViewById(R.id.open_pdf_mupdfreaderview);

        initToolsView();

        mAlertBuilder  = new AlertDialog.Builder(this);
        //  keep a static copy of this that other classes can use
        gAlertBuilder = mAlertBuilder;

        String path = filePath;

        // ??????MuPDFCore??????pdf??????
        muPDFCore = openFile(path);
        // ???????????????
        SearchTaskResult.set(null);
        // ????????????core?????????????????????????????????
        if (muPDFCore == null) {
            AlertDialog alert = mAlertBuilder.create();
            alert.setTitle(com.lonelypluto.pdflibrary.R.string.cannot_open_document);
            alert.setButton(AlertDialog.BUTTON_POSITIVE, getString(com.lonelypluto.pdflibrary.R.string.dismiss),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
            alert.setOnCancelListener(new DialogInterface.OnCancelListener() {

                @Override
                public void onCancel(DialogInterface dialog) {
                    finish();
                }
            });
            alert.show();
            return;
        }

        createUI();

        //??????????????????
        btn_change_hv = (Button)findViewById(R.id.btn_change_hv);
        btn_change_hv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ischangeHV) {
                    muPDFReaderView.setHorizontalScrolling(ischangeHV);
                    btn_change_hv.setText("???");
                    ischangeHV = false;
                } else {
                    muPDFReaderView.setHorizontalScrolling(ischangeHV);
                    btn_change_hv.setText("???");
                    ischangeHV = true;
                }
            }
        });

        // ?????????????????????
        btn_linkhighlightcolor = (Button)findViewById(R.id.btn_linkhighlightcolor);
        btn_linkhighlightcolor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setLinkHighlightColor(ContextCompat.getColor(AllPDFActivity.this, com.lonelypluto.pdflibrary.R.color.link_bg));
            }
        });

        // ????????????????????????
        btn_searchtextcolor = (Button)findViewById(R.id.btn_searchtextcolor);
        btn_searchtextcolor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSearchTextColor(ContextCompat.getColor(AllPDFActivity.this, com.lonelypluto.pdflibrary.R.color.search_bg));
            }
        });

        // ??????????????????
        btn_paintcolor = (Button)findViewById(R.id.btn_set_paint_color);
        btn_paintcolor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int c = ContextCompat.getColor(AllPDFActivity.this, com.lonelypluto.pdflibrary.R.color.rv_item_line_bg);
                Log.e(TAG, "color = " + c);
                setColor(c);
                setInkColor(0xFF0000FF);
            }
        });

        // ??????????????????
        btn_paintstrokewidth = (Button)findViewById(R.id.btn_set_paint_strokewidth);
        btn_paintstrokewidth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPaintStrockWidth(20.0f);
            }
        });

        // ????????????
        btn_sign = (Button)findViewById(R.id.btn_sign);
        btn_sign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, "btn_sign");
                vdhDeepLayout.setVisibility(View.VISIBLE);
            }
        });

        // ??????
        btn_save = (Button)findViewById(R.id.btn_save);
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, "btn_save");
                String in_path = filePath;
                String out_path = in_path.substring(0, in_path.length() - 4) + "_t2.pdf";
                SavePdf savePdf = new SavePdf(in_path, out_path);
                savePdf.setScale(muPDFReaderView.getCurrentScale());
                savePdf.setPageNum(muPDFReaderView.getDisplayedViewIndex() + 1);

                savePdf.setWidthScale(1.0f * muPDFReaderView.getScaleX() / muPDFReaderView.getDisplayedView().getWidth());//???????????????????????????
                savePdf.setHeightScale(1.0f * muPDFReaderView.getScaleY() / muPDFReaderView.getDisplayedView().getHeight());//???????????????????????????

                Log.e(TAG, "scaleX = " + muPDFReaderView.getScaleX() + "   " + muPDFReaderView.getDisplayedView().getWidth());
                savePdf.setWH(iv_sign.getX(), iv_sign.getY());
//                savePdf.setWidthScale(0);
//                savePdf.setHeightScale(0);

                //?????????????????????
                DisplayMetrics metric = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(metric);

                float density = metric.density;
                savePdf.setDensity(density);

                Bitmap bitmap = getBitmap(AllPDFActivity.this, com.lonelypluto.pdflibrary.R.mipmap.ic_launcher);
                savePdf.setBitmap(bitmap);

//                Bitmap bitmap = Bitmap.createBitmap(vdhDeepLayout.getWidth(), vdhDeepLayout.getHeight(),
//                        Bitmap.Config.ARGB_8888);
//                Canvas canvas = new Canvas(bitmap);
//                vdhDeepLayout.draw(canvas);
//                savePdf.setBitmap(bitmap);

                Log.e(TAG, "iv_p = " + iv_sign.getX() + " " + iv_sign.getY());
                Log.e(TAG, "?????? = " + vdhDeepLayout.getWidth() + " " + vdhDeepLayout.getHeight());
                savePdfTask = new SavePdfTask(savePdf);
                savePdfTask.execute();
            }
        });

        vdhDeepLayout = (VDHDeepLayout)findViewById(R.id.VDHDeepLayout);
        iv_sign = (ImageView)findViewById(R.id.iv_sign);

        iv_test = (ImageView)findViewById(R.id.iv_test);
    }

    private static Bitmap getBitmap(Context context,int vectorDrawableId) {
        Bitmap bitmap=null;
        if (Build.VERSION.SDK_INT>Build.VERSION_CODES.LOLLIPOP){
            Drawable vectorDrawable = context.getDrawable(vectorDrawableId);
            bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                    vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            vectorDrawable.draw(canvas);
        }else {
            bitmap = BitmapFactory.decodeResource(context.getResources(), vectorDrawableId);
        }
        return bitmap;
    }

    public void setColor(int color) {
        int red = (color & 0xff0000) >> 16;
        int green = (color & 0x00ff00) >> 8;
        int blue = (color & 0x0000ff);
        Log.e(TAG, "r = " + red + " g = " + green + " b = " + blue);
    }

    /**
     * ??????????????????
     */
    private void initToolsView() {

        mTopBarSwitcher = (ViewAnimator) findViewById(R.id.switcher);
        mLinkButton = (ImageButton) findViewById(R.id.linkButton);
        mAnnotButton = (ImageButton) findViewById(R.id.reflowButton);
        mOutlineButton = (ImageButton) findViewById(R.id.outlineButton);
        mSearchButton = (ImageButton) findViewById(R.id.searchButton);

        mSearchText = (EditText) findViewById(R.id.searchText);
        mSearchBack = (ImageButton) findViewById(R.id.searchBack);
        mSearchFwd = (ImageButton) findViewById(R.id.searchForward);

        mAnnotTypeText = (TextView) findViewById(R.id.annotType);

        mPageNumberView = (TextView) findViewById(R.id.pageNumber);
        mPageSlider = (SeekBar) findViewById(R.id.pageSlider);

        mTopBarSwitcher.setVisibility(View.INVISIBLE);
        mPageNumberView.setVisibility(View.INVISIBLE);
        mPageSlider.setVisibility(View.INVISIBLE);
    }

    /**
     * ????????????
     * @param path ????????????
     * @return
     */
    private MuPDFCore openFile(String path) {
        int lastSlashPos = path.lastIndexOf('/');
        fileName = new String(lastSlashPos == -1
                ? path
                : path.substring(lastSlashPos + 1));
        Log.e(TAG, "filename = " + fileName);
        Log.e(TAG, "Trying to open " + path);
        try {
            muPDFCore = new MuPDFCore(this, path);
            // ?????????????????????????????????
            OutlineActivityData.set(null);
        } catch (Exception e) {
            Log.e(TAG, "openFile catch:" + e.toString());
            return null;
        } catch (OutOfMemoryError e) {
            //  out of memory is not an Exception, so we catch it separately.
            Log.e(TAG, "openFile catch: OutOfMemoryError " + e.toString());
            return null;
        }
        return muPDFCore;
    }

    private void createUI() {
        if (muPDFCore == null)
            return;
        // Set up the page slider
        int smax = Math.max(muPDFCore.countPages() - 1, 1);
        mPageSliderRes = ((10 + smax - 1) / smax) * 2;

        // Now create the UI.
        // First create the document view

        muPDFReaderView.setListener(new MuPDFReaderViewListener() {
            @Override
            public void onMoveToChild(int i) {
                if (muPDFCore == null)
                    return;

                mPageNumberView.setText(String.format("%d / %d", i + 1,
                        muPDFCore.countPages()));
                mPageSlider.setMax((muPDFCore.countPages() - 1) * mPageSliderRes);
                mPageSlider.setProgress(i * mPageSliderRes);
            }

            @Override
            public void onTapMainDocArea() {
                if (!mButtonsVisible) {
                    showButtons();
                } else {
                    if (mTopBarMode == TopBarMode.Main)
                        hideButtons();
                }
            }

            @Override
            public void onDocMotion() {
                hideButtons();
            }

            @Override
            public void onHit(Hit item) {
                switch (mTopBarMode) {
                    case Annot:
                        if (item == Hit.Annotation) {
                            showButtons();
                            mTopBarMode = TopBarMode.Delete;
                            mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
                        }
                        break;
                    case Delete:
                        mTopBarMode = TopBarMode.Annot;
                        mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
                        // fall through
                    default:
                        // Not in annotation editing mode, but the pageview will
                        // still select and highlight hit annotations, so
                        // deselect just in case.
                        MuPDFView pageView = (MuPDFView) muPDFReaderView.getDisplayedView();
                        if (pageView != null)
                            pageView.deselectAnnotation();
                        break;
                }
            }
        });

        muPDFReaderView.setAdapter(new MuPDFPageAdapter(this, muPDFCore));
        // ??????view????????????
        muPDFReaderView.setBackgroundColor(ContextCompat.getColor(this, com.lonelypluto.pdflibrary.R.color.muPDFReaderView_bg));

        mSearchTask = new SearchTask(this, muPDFCore) {
            @Override
            protected void onTextFound(SearchTaskResult result) {
                SearchTaskResult.set(result);
                // Ask the ReaderView to move to the resulting page
                muPDFReaderView.setDisplayedViewIndex(result.pageNumber);
                // Make the ReaderView act on the change to SearchTaskResult
                // via overridden onChildSetup method.
                muPDFReaderView.resetupChildren();
            }
        };

        mPageSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onStopTrackingTouch(SeekBar seekBar) {
                muPDFReaderView.setDisplayedViewIndex((seekBar.getProgress() + mPageSliderRes / 2) / mPageSliderRes);
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                updatePageNumView((progress + mPageSliderRes / 2) / mPageSliderRes);
            }
        });

        // Activate the search-preparing button
        mSearchButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                searchModeOn();
            }
        });


        if (muPDFCore.fileFormat().startsWith("PDF") && muPDFCore.isUnencryptedPDF() && !muPDFCore.wasOpenedFromBuffer()) {
            mAnnotButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    mTopBarMode = TopBarMode.Annot;
                    mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
                }
            });
        } else {
            mAnnotButton.setVisibility(View.GONE);
        }

        // Search invoking buttons are disabled while there is no text specified
        mSearchBack.setEnabled(false);
        mSearchFwd.setEnabled(false);
        mSearchBack.setColorFilter(Color.argb(0xFF, 250, 250, 250));
        mSearchFwd.setColorFilter(Color.argb(0xFF, 250, 250, 250));

        // React to interaction with the text widget
        mSearchText.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
                boolean haveText = s.toString().length() > 0;
                setButtonEnabled(mSearchBack, haveText);
                setButtonEnabled(mSearchFwd, haveText);

                // Remove any previous search results
                if (SearchTaskResult.get() != null && !mSearchText.getText().toString().equals(SearchTaskResult.get().txt)) {
                    SearchTaskResult.set(null);
                    muPDFReaderView.resetupChildren();
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
            }
        });

        //React to Done button on keyboard
        mSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE)
                    search(1);
                return false;
            }
        });

        mSearchText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER)
                    search(1);
                return false;
            }
        });

        // Activate search invoking buttons
        mSearchBack.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                search(-1);
            }
        });
        mSearchFwd.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                search(1);
            }
        });

        mLinkButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setLinkHighlight(!mLinkHighlight);
            }
        });

        // ????????????pdf???????????????
        if (muPDFCore.hasOutline()) {
            // ????????????????????????????????????
            mOutlineButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    OutlineItem outline[] = muPDFCore.getOutline();
                    if (outline != null) {
                        OutlineActivityData.get().items = outline;
                        Intent intent = new Intent(AllPDFActivity.this, OutlineActivity.class);
                        startActivityForResult(intent, OUTLINE_REQUEST);
                    }
                }
            });
        } else {
            mOutlineButton.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case OUTLINE_REQUEST:
                if (resultCode >= 0)
                    muPDFReaderView.setDisplayedViewIndex(resultCode);
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * ???????????????
     */
    private void showButtons() {
        if (muPDFCore == null)
            return;
        if (!mButtonsVisible) {
            mButtonsVisible = true;
            // Update page number text and slider
            int index = muPDFReaderView.getDisplayedViewIndex();
            updatePageNumView(index);
            mPageSlider.setMax((muPDFCore.countPages() - 1) * mPageSliderRes);
            mPageSlider.setProgress(index * mPageSliderRes);
            if (mTopBarMode == TopBarMode.Search) {
                mSearchText.requestFocus();
                showKeyboard();
            }

            Animation anim = new TranslateAnimation(0, 0, -mTopBarSwitcher.getHeight(), 0);
            anim.setDuration(200);
            anim.setAnimationListener(new Animation.AnimationListener() {
                public void onAnimationStart(Animation animation) {
                    mTopBarSwitcher.setVisibility(View.VISIBLE);
                }

                public void onAnimationRepeat(Animation animation) {
                }

                public void onAnimationEnd(Animation animation) {
                }
            });
            mTopBarSwitcher.startAnimation(anim);

            anim = new TranslateAnimation(0, 0, mPageSlider.getHeight(), 0);
            anim.setDuration(200);
            anim.setAnimationListener(new Animation.AnimationListener() {
                public void onAnimationStart(Animation animation) {
                    mPageSlider.setVisibility(View.VISIBLE);
                }

                public void onAnimationRepeat(Animation animation) {
                }

                public void onAnimationEnd(Animation animation) {
                    mPageNumberView.setVisibility(View.VISIBLE);
                }
            });
            mPageSlider.startAnimation(anim);
        }
    }

    /**
     * ???????????????
     */
    private void hideButtons() {
        if (mButtonsVisible) {
            mButtonsVisible = false;
            hideKeyboard();

            Animation anim = new TranslateAnimation(0, 0, 0, -mTopBarSwitcher.getHeight());
            anim.setDuration(200);
            anim.setAnimationListener(new Animation.AnimationListener() {
                public void onAnimationStart(Animation animation) {
                }

                public void onAnimationRepeat(Animation animation) {
                }

                public void onAnimationEnd(Animation animation) {
                    mTopBarSwitcher.setVisibility(View.INVISIBLE);
                }
            });
            mTopBarSwitcher.startAnimation(anim);

            anim = new TranslateAnimation(0, 0, 0, mPageSlider.getHeight());
            anim.setDuration(200);
            anim.setAnimationListener(new Animation.AnimationListener() {
                public void onAnimationStart(Animation animation) {
                    mPageNumberView.setVisibility(View.INVISIBLE);
                }

                public void onAnimationRepeat(Animation animation) {
                }

                public void onAnimationEnd(Animation animation) {
                    mPageSlider.setVisibility(View.INVISIBLE);
                }
            });
            mPageSlider.startAnimation(anim);
        }
    }

    /**
     * ???????????????????????????
     * @param index
     */
    private void updatePageNumView(int index) {
        if (muPDFCore == null)
            return;
        mPageNumberView.setText(String.format("%d / %d", index + 1, muPDFCore.countPages()));
    }

    /**
     * ????????????
     */
    private void showKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null)
            imm.showSoftInput(mSearchText, 0);
    }

    /**
     * ????????????
     */
    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null)
            imm.hideSoftInputFromWindow(mSearchText.getWindowToken(), 0);
    }

    /**
     * ????????? - ??????????????????
     * @param v
     */
    public void OnEditAnnotButtonClick(View v) {
        mTopBarMode = TopBarMode.Main;
        mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
    }

    /**
     * ????????? - ??????????????????
     * @param v
     */
    public void OnCopyTextButtonClick(View v) {
        mTopBarMode = TopBarMode.Accept;
        mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
        mAcceptMode = AcceptMode.CopyText;
        muPDFReaderView.setMode(MuPDFReaderView.Mode.Selecting);
        mAnnotTypeText.setText(getString(com.lonelypluto.pdflibrary.R.string.copy_text));
        showInfo(getString(com.lonelypluto.pdflibrary.R.string.select_text));
    }

    /**
     * ????????? - ???????????????????????????
     * @param v
     */
    public void OnCancelSearchButtonClick(View v) {
        searchModeOff();
    }

    /**
     * ????????? - ????????????????????????
     * @param v
     */
    public void OnCancelMoreButtonClick(View v) {
        mTopBarMode = TopBarMode.Main;
        mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
    }

    /**
     * ????????????
     */
    private void searchModeOn() {
        if (mTopBarMode != TopBarMode.Search) {
            mTopBarMode = TopBarMode.Search;
            //Focus on EditTextWidget
            mSearchText.requestFocus();
            showKeyboard();
            mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
        }
    }

    /**
     * ????????????
     */
    private void searchModeOff() {
        if (mTopBarMode == TopBarMode.Search) {
            mTopBarMode = TopBarMode.Main;
            hideKeyboard();
            mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
            SearchTaskResult.set(null);
            // Make the ReaderView act on the change to mSearchTaskResult
            // via overridden onChildSetup method.
            muPDFReaderView.resetupChildren();
        }
    }

    /**
     * ????????? - ?????? - ??????????????????
     * @param v
     */
    public void OnHighlightButtonClick(View v) {
        mTopBarMode = TopBarMode.Accept;
        mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
        mAcceptMode = AcceptMode.Highlight;
        muPDFReaderView.setMode(MuPDFReaderView.Mode.Selecting);
        mAnnotTypeText.setText(com.lonelypluto.pdflibrary.R.string.pdf_tools_highlight);
        showInfo(getString(com.lonelypluto.pdflibrary.R.string.select_text));
    }

    /**
     * ????????? - ?????? - ????????????????????????
     * @param v
     */
    public void OnUnderlineButtonClick(View v) {
        mTopBarMode = TopBarMode.Accept;
        mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
        mAcceptMode = AcceptMode.Underline;
        muPDFReaderView.setMode(MuPDFReaderView.Mode.Selecting);
        mAnnotTypeText.setText(com.lonelypluto.pdflibrary.R.string.pdf_tools_underline);
        showInfo(getString(com.lonelypluto.pdflibrary.R.string.select_text));
    }

    /**
     * ????????? - ?????? - ?????????????????????
     * @param v
     */
    public void OnStrikeOutButtonClick(View v) {
        mTopBarMode = TopBarMode.Accept;
        mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
        mAcceptMode = AcceptMode.StrikeOut;
        muPDFReaderView.setMode(MuPDFReaderView.Mode.Selecting);
        mAnnotTypeText.setText(com.lonelypluto.pdflibrary.R.string.pdf_tools_strike_out);
        showInfo(getString(com.lonelypluto.pdflibrary.R.string.select_text));
    }

    /**
     * ????????? - ?????? - ??????????????????
     * @param v
     */
    public void OnInkButtonClick(View v) {
        mTopBarMode = TopBarMode.Accept;
        mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
        mAcceptMode = AcceptMode.Ink;
        muPDFReaderView.setMode(MuPDFReaderView.Mode.Drawing);
        mAnnotTypeText.setText(com.lonelypluto.pdflibrary.R.string.pdf_tools_ink);
        showInfo(getString(com.lonelypluto.pdflibrary.R.string.pdf_tools_draw_annotation));
    }

    /**
     * ????????? - ?????? - ????????????????????????
     * @param v
     */
    public void OnDeleteButtonClick(View v) {
        MuPDFView pageView = (MuPDFView) muPDFReaderView.getDisplayedView();
        if (pageView != null)
            pageView.deleteSelectedAnnotation();
        mTopBarMode = TopBarMode.Annot;
        mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
    }

    /**
     * ????????? - ?????? - ??????????????????????????????
     * @param v
     */
    public void OnCancelDeleteButtonClick(View v) {
        MuPDFView pageView = (MuPDFView) muPDFReaderView.getDisplayedView();
        if (pageView != null)
            pageView.deselectAnnotation();
        mTopBarMode = TopBarMode.Annot;
        mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
    }

    /**
     * ????????? - ?????? - ??????????????????
     * @param v
     */
    public void OnCancelAcceptButtonClick(View v) {
        MuPDFView pageView = (MuPDFView) muPDFReaderView.getDisplayedView();
        if (pageView != null) {
            pageView.deselectText();
            pageView.cancelDraw();
        }
        muPDFReaderView.setMode(MuPDFReaderView.Mode.Viewing);
        switch (mAcceptMode) {
            case CopyText:
                mTopBarMode = TopBarMode.Main;
                break;
            default:
                mTopBarMode = TopBarMode.Annot;
                break;
        }
        mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
    }

    /**
     * ????????? - ?????? - ??????????????????
     * @param v
     */
    public void OnAcceptButtonClick(View v) {
        MuPDFView pageView = (MuPDFView) muPDFReaderView.getDisplayedView();
        boolean success = false;
        switch (mAcceptMode) {
            case CopyText:
                if (pageView != null)
                    success = pageView.copySelection();
                mTopBarMode = TopBarMode.Main;
                showInfo(success ? getString(com.lonelypluto.pdflibrary.R.string.copied_to_clipboard) : getString(com.lonelypluto.pdflibrary.R.string.no_text_selected));
                break;
            case Highlight:
                // ??????
                if (pageView != null) {
                    success = pageView.markupSelection(Annotation.Type.HIGHLIGHT);
                }
                mTopBarMode = TopBarMode.Annot;
                if (!success) {
                    showInfo(getString(com.lonelypluto.pdflibrary.R.string.no_text_selected));
                }
                break;
            case Underline:
                if (pageView != null)
                    success = pageView.markupSelection(Annotation.Type.UNDERLINE);
                mTopBarMode = TopBarMode.Annot;
                if (!success)
                    showInfo(getString(com.lonelypluto.pdflibrary.R.string.no_text_selected));
                break;

            case StrikeOut:
                if (pageView != null)
                    success = pageView.markupSelection(Annotation.Type.STRIKEOUT);
                mTopBarMode = TopBarMode.Annot;
                if (!success)
                    showInfo(getString(com.lonelypluto.pdflibrary.R.string.no_text_selected));
                break;

            case Ink:
                if (pageView != null)
                    success = pageView.saveDraw();
                mTopBarMode = TopBarMode.Annot;
                if (!success)
                    showInfo(getString(com.lonelypluto.pdflibrary.R.string.nothing_to_save));
                break;
        }
        mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
        muPDFReaderView.setMode(MuPDFReaderView.Mode.Viewing);
    }

    /**
     * ???????????????????????????
     * @param button
     * @param enabled
     */
    private void setButtonEnabled(ImageButton button, boolean enabled) {
        button.setEnabled(enabled);
        button.setColorFilter(enabled ? Color.argb(0xFF, 250, 250, 250) : Color.argb(0xFF, 250, 250, 250));
    }

    /**
     * ????????????
     * @param direction ????????????
     */
    private void search(int direction) {
        hideKeyboard();
        int displayPage = muPDFReaderView.getDisplayedViewIndex();
        SearchTaskResult r = SearchTaskResult.get();
        int searchPage = r != null ? r.pageNumber : -1;
        mSearchTask.go(mSearchText.getText().toString(), direction, displayPage, searchPage);
    }

    /**
     * ???????????????????????????
     * @param highlight
     */
    private void setLinkHighlight(boolean highlight) {
        mLinkHighlight = highlight;
        // LINK_COLOR tint
        mLinkButton.setColorFilter(highlight ? Color.argb(0xFF, 255, 160, 0) : Color.argb(0xFF, 255, 255, 255));
        // Inform pages of the change.
        muPDFReaderView.setLinksEnabled(highlight);
    }

    /**
     * ?????????????????????
     * @param color ?????????
     */
    private void setLinkHighlightColor(int color) {
        muPDFReaderView.setLinkHighlightColor(color);
    }

    /**
     * ????????????????????????
     * @param color ?????????
     */
    private void setSearchTextColor(int color) {
        muPDFReaderView.setSearchTextColor(color);
    }

    /**
     * ??????????????????
     * @param color ?????????
     */
    private void setInkColor(int color) {
        muPDFReaderView.setInkColor(color);
    }

    /**
     * ??????????????????
     * @param inkThickness ?????????
     */
    private void setPaintStrockWidth(float inkThickness) {
        muPDFReaderView.setPaintStrockWidth(inkThickness);
    }

    /**
     * ???????????????????????????
     * @param message ????????????
     */
    private void showInfo(String message) {

        LayoutInflater inflater = getLayoutInflater();
        View toastLayout = inflater.inflate(com.lonelypluto.pdflibrary.R.layout.toast,
                (ViewGroup) findViewById(com.lonelypluto.pdflibrary.R.id.toast_root_view));

        TextView header = (TextView) toastLayout.findViewById(com.lonelypluto.pdflibrary.R.id.toast_message);
        header.setText(message);

        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.FILL_HORIZONTAL | Gravity.BOTTOM, 0, 0);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(toastLayout);
        toast.show();
    }

    /**
     * ????????????dialog
     * @return
     */
    public static AlertDialog.Builder getAlertBuilder() {
        return gAlertBuilder;
    }

    /**
     * ??????????????????
     */
    public void createAlertWaiter() {
        mAlertsActive = true;
        // All mupdf library calls are performed on asynchronous tasks to avoid stalling
        // the UI. Some calls can lead to javascript-invoked requests to display an
        // alert dialog and collect a reply from the user. The task has to be blocked
        // until the user's reply is received. This method creates an asynchronous task,
        // the purpose of which is to wait of these requests and produce the dialog
        // in response, while leaving the core blocked. When the dialog receives the
        // user's response, it is sent to the core via replyToAlert, unblocking it.
        // Another alert-waiting task is then created to pick up the next alert.
        if (mAlertTask != null) {
            mAlertTask.cancel(true);
            mAlertTask = null;
        }
        if (mAlertDialog != null) {
            mAlertDialog.cancel();
            mAlertDialog = null;
        }
        mAlertTask = new AsyncTask<Void, Void, MuPDFAlert>() {

            @Override
            protected MuPDFAlert doInBackground(Void... arg0) {
                if (!mAlertsActive)
                    return null;

                return muPDFCore.waitForAlert();
            }

            @Override
            protected void onPostExecute(final MuPDFAlert result) {
                // core.waitForAlert may return null when shutting down
                if (result == null)
                    return;
                final MuPDFAlert.ButtonPressed pressed[] = new MuPDFAlert.ButtonPressed[3];
                for (int i = 0; i < 3; i++)
                    pressed[i] = MuPDFAlert.ButtonPressed.None;
                DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mAlertDialog = null;
                        if (mAlertsActive) {
                            int index = 0;
                            switch (which) {
                                case AlertDialog.BUTTON1:
                                    index = 0;
                                    break;
                                case AlertDialog.BUTTON2:
                                    index = 1;
                                    break;
                                case AlertDialog.BUTTON3:
                                    index = 2;
                                    break;
                            }
                            result.buttonPressed = pressed[index];
                            // Send the user's response to the core, so that it can
                            // continue processing.
                            muPDFCore.replyToAlert(result);
                            // Create another alert-waiter to pick up the next alert.
                            createAlertWaiter();
                        }
                    }
                };
                mAlertDialog = mAlertBuilder.create();
                mAlertDialog.setTitle(result.title);
                mAlertDialog.setMessage(result.message);
                switch (result.iconType) {
                    case Error:
                        break;
                    case Warning:
                        break;
                    case Question:
                        break;
                    case Status:
                        break;
                }
                switch (result.buttonGroupType) {
                    case OkCancel:
                        mAlertDialog.setButton(AlertDialog.BUTTON2, getString(com.lonelypluto.pdflibrary.R.string.cancel), listener);
                        pressed[1] = MuPDFAlert.ButtonPressed.Cancel;
                    case Ok:
                        mAlertDialog.setButton(AlertDialog.BUTTON1, getString(com.lonelypluto.pdflibrary.R.string.okay), listener);
                        pressed[0] = MuPDFAlert.ButtonPressed.Ok;
                        break;
                    case YesNoCancel:
                        mAlertDialog.setButton(AlertDialog.BUTTON3, getString(com.lonelypluto.pdflibrary.R.string.cancel), listener);
                        pressed[2] = MuPDFAlert.ButtonPressed.Cancel;
                    case YesNo:
                        mAlertDialog.setButton(AlertDialog.BUTTON1, getString(com.lonelypluto.pdflibrary.R.string.yes), listener);
                        pressed[0] = MuPDFAlert.ButtonPressed.Yes;
                        mAlertDialog.setButton(AlertDialog.BUTTON2, getString(com.lonelypluto.pdflibrary.R.string.no), listener);
                        pressed[1] = MuPDFAlert.ButtonPressed.No;
                        break;
                }
                mAlertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    public void onCancel(DialogInterface dialog) {
                        mAlertDialog = null;
                        if (mAlertsActive) {
                            result.buttonPressed = MuPDFAlert.ButtonPressed.None;
                            muPDFCore.replyToAlert(result);
                            createAlertWaiter();
                        }
                    }
                });

                mAlertDialog.show();
            }
        };

        mAlertTask.executeOnExecutor(new ThreadPerTaskExecutor());
    }

    /**
     * ??????????????????
     */
    public void destroyAlertWaiter() {
        mAlertsActive = false;
        if (mAlertDialog != null) {
            mAlertDialog.cancel();
            mAlertDialog = null;
        }
        if (mAlertTask != null) {
            mAlertTask.cancel(true);
            mAlertTask = null;
        }
    }

    @Override
    protected void onStart() {
        if (muPDFCore != null) {
            muPDFCore.startAlerts();
            createAlertWaiter();
        }
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mSearchTask != null) {
            mSearchTask.stop();
        }

//        if (mFileName != null && mDocView != null) {
//            SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
//            SharedPreferences.Editor edit = prefs.edit();
//            edit.putInt("page" + mFileName, mDocView.getDisplayedViewIndex());
//            edit.commit();
//        }
    }

    @Override
    protected void onStop() {
        if (muPDFCore != null) {
            destroyAlertWaiter();
            muPDFCore.stopAlerts();
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (muPDFReaderView != null) {
            muPDFReaderView.applyToChildren(new ReaderView.ViewMapper() {
                public void applyToView(View view) {
                    ((MuPDFView) view).releaseBitmaps();
                }
            });
        }
        if (muPDFCore != null)
            muPDFCore.onDestroy();
        if (mAlertTask != null) {
            mAlertTask.cancel(true);
            mAlertTask = null;
        }
        muPDFCore = null;
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (muPDFCore != null && muPDFCore.hasChanges()) {
            DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (which == AlertDialog.BUTTON_POSITIVE) {
                        muPDFCore.save();
                    }
                    finish();
                }
            };
            AlertDialog alert = mAlertBuilder.create();
            alert.setTitle(com.lonelypluto.pdflibrary.R.string.dialog_title);
            alert.setMessage(getString(com.lonelypluto.pdflibrary.R.string.document_has_changes_save_them));
            alert.setButton(AlertDialog.BUTTON_POSITIVE, getString(com.lonelypluto.pdflibrary.R.string.yes), listener);
            alert.setButton(AlertDialog.BUTTON_NEGATIVE, getString(com.lonelypluto.pdflibrary.R.string.no), listener);
            alert.show();
        } else {
            finish();
        }
    }

    /**
     * ????????????
     */
    class ThreadPerTaskExecutor implements Executor {
        public void execute(Runnable r) {
            new Thread(r).start();
        }
    }

    /**
     * ??????????????? Search:?????? Annot:??????
     */
    enum TopBarMode {
        Main, Search, Annot, Delete, Accept
    }

    /**
     * ????????????????????? Highlight:???????????? ,Underline:???????????? ,StrikeOut:????????? ,Ink:?????? ,CopyText:????????????
     */
    enum AcceptMode {
        Highlight, Underline, StrikeOut, Ink, CopyText
    }
}