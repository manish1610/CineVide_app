package com.oxoo.spagreen;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.mediarouter.app.MediaRouteButton;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import at.huber.youtubeExtractor.VideoMeta;
import at.huber.youtubeExtractor.YouTubeExtractor;
import at.huber.youtubeExtractor.YtFile;

import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ext.cast.CastPlayer;
import com.google.android.exoplayer2.ext.rtmp.RtmpDataSourceFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MergingMediaSource;
import com.google.android.exoplayer2.source.SingleSampleMediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlaybackControlView;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.ui.SubtitleView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.Util;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.MediaQueueItem;
import com.google.android.gms.cast.MediaStatus;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.CastState;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;
import com.google.android.gms.common.images.WebImage;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.oxoo.spagreen.adapters.CastCrewAdapter;
import com.oxoo.spagreen.adapters.CommentsAdapter;
import com.oxoo.spagreen.adapters.DirectorApater;
import com.oxoo.spagreen.adapters.DownloadAdapter;
import com.oxoo.spagreen.adapters.EpisodeAdapter;
import com.oxoo.spagreen.adapters.HomePageAdapter;
import com.oxoo.spagreen.adapters.LiveTvHomeAdapter;
import com.oxoo.spagreen.adapters.ProgramAdapter;
import com.oxoo.spagreen.adapters.ServerApater;
import com.oxoo.spagreen.models.CastCrew;
import com.oxoo.spagreen.models.CommentsModel;
import com.oxoo.spagreen.models.CommonModels;
import com.oxoo.spagreen.models.EpiModel;
import com.oxoo.spagreen.models.Program;
import com.oxoo.spagreen.models.SubtitleModel;
import com.oxoo.spagreen.utils.ApiResources;
import com.oxoo.spagreen.utils.BannerAds;
import com.oxoo.spagreen.utils.Constants;
import com.oxoo.spagreen.utils.PopUpAds;
import com.oxoo.spagreen.utils.ToastMsg;
import com.oxoo.spagreen.utils.Tools;
import com.oxoo.spagreen.utils.VolleySingleton;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.google.android.gms.ads.AdActivity.CLASS_NAME;

public class DetailsActivity extends AppCompatActivity implements CastPlayer.SessionAvailabilityListener, ProgramAdapter.OnProgramClickListener {

    private static final int PERMISSION_REQUEST_CODE = 1;
    private static final int PRELOAD_TIME_S = 20;

    public static final String TAG = DetailsActivity.class.getSimpleName();

    private TextView tvName, tvDirector, tvRelease, tvCast, tvDes, tvGenre, tvRelated;


    private RecyclerView rvDirector, rvServer, rvRelated, rvComment, rvDownload, castRv;

    public static RelativeLayout lPlay;

    private ServerApater serverAdapter;
    private DownloadAdapter downloadAdapter;
    private EpisodeAdapter episodeAdapter;
    private HomePageAdapter relatedAdapter;
    private LiveTvHomeAdapter relatedTvAdapter;
    private CastCrewAdapter castCrewAdapter;

    int start = 0;


    private List<CommonModels> listDirector = new ArrayList<>();
    private List<CommonModels> listEpisode = new ArrayList<>();
    private List<CommonModels> listRelated = new ArrayList<>();
    private List<CommentsModel> listComment = new ArrayList<>();
    private List<CommonModels> listDownload = new ArrayList<>();
    private List<CastCrew> castCrews = new ArrayList<>();


    private String strDirector = "", strCast = "", strGenre = "";
    public static LinearLayout llBottom, llBottomParent, llcomment;

    private SwipeRefreshLayout swipeRefreshLayout;

    private String type = "", id = "";

    private ImageView imgAddFav;

    public static ImageView imgBack;

    private String V_URL = "";
    public static WebView webView;
    public static ProgressBar progressBar;
    private boolean isFav = false;

    private TextView chromeCastTv;


    private ShimmerFrameLayout shimmerFrameLayout;

    private Button btnComment;
    private EditText etComment;
    private CommentsAdapter commentsAdapter;

    private String commentURl;
    private RelativeLayout adView;
    private InterstitialAd mInterstitialAd;
    private LinearLayout download_text;


    public static SimpleExoPlayer player;
    public static PlayerView simpleExoPlayerView;
    public PlayerControlView castControlView;
    public static SubtitleView subtitleView;

    public static ImageView imgFull, downloadIv;
    public MediaRouteButton mediaRouteButton;
    private CastContext castContext;

    public static boolean isPlaying, isFullScr;
    public static View playerLayout;

    private int playerHeight;
    public static boolean isVideo = true;
    private FirebaseAnalytics mFirebaseAnalytics;
    private String strSubtitle = "Null";
    public static MediaSource mediaSource = null;
    public static ImageView imgSubtitle;
    private List<SubtitleModel> listSub = new ArrayList<>();
    private AlertDialog alertDialog;
    private String mediaUrl;
    private boolean tv = false;
    private String download_check = "";

    private String season;
    private String episod;
    private String movieTitle;
    private String seriesTitle;

    private CastPlayer castPlayer;
    private boolean castSession;
    private String title;
    String castImageUrl;

    private LinearLayout tvLayout, sheduleLayout, tvTopLayout;
    private TextView tvTitleTv, watchStatusTv, timeTv, programTv, proGuideTv, watchLiveTv;
    private ProgramAdapter programAdapter;
    List<Program> programs = new ArrayList<>();
    private RecyclerView programRv;
    private ImageView tvThumbIv, shareIv;

    private LinearLayout exoRewind, exoForward, seekbarLayout;
    private TextView liveTv;


    boolean isDark;
    private OrientationEventListener myOrientationEventListener;
    private String serverType;

    private boolean fullScreenByClick;
    private String currentProgramTime;
    private String currentProgramTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        SharedPreferences sharedPreferences = getSharedPreferences("push", MODE_PRIVATE);
         isDark = sharedPreferences.getBoolean("dark", false);

        if (isDark) {
            setTheme(R.style.AppThemeDark);
        } else {
            setTheme(R.style.AppThemeLight);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);



        //---analytics-----------
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "id");
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "details_activity");
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "activity");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

        adView = findViewById(R.id.adView);
        llBottom = findViewById(R.id.llbottom);
        tvDes = findViewById(R.id.tv_details);
        tvCast = findViewById(R.id.tv_cast);
        tvRelease = findViewById(R.id.tv_release_date);
        tvName = findViewById(R.id.text_name);
        tvDirector = findViewById(R.id.tv_director);
        tvGenre = findViewById(R.id.tv_genre);
        swipeRefreshLayout = findViewById(R.id.swipe_layout);
        imgAddFav = findViewById(R.id.add_fav);
        imgBack = findViewById(R.id.img_back);
        webView = findViewById(R.id.webView);
        progressBar = findViewById(R.id.progressBar);
        llBottomParent = findViewById(R.id.llbottomparent);
        lPlay = findViewById(R.id.play);
        rvRelated = findViewById(R.id.rv_related);
        tvRelated = findViewById(R.id.tv_related);
        shimmerFrameLayout = findViewById(R.id.shimmer_view_container);
        btnComment = findViewById(R.id.btn_comment);
        etComment = findViewById(R.id.et_comment);
        rvComment = findViewById(R.id.recyclerView_comment);
        llcomment = findViewById(R.id.llcomments);
        simpleExoPlayerView = findViewById(R.id.video_view);
        subtitleView = findViewById(R.id.subtitle);
        playerLayout = findViewById(R.id.player_layout);
        imgFull = findViewById(R.id.img_full_scr);
        downloadIv = findViewById(R.id.img_download);
        rvServer = findViewById(R.id.rv_server_list);
        rvDownload = findViewById(R.id.rv_download_list);
        imgSubtitle = findViewById(R.id.img_subtitle);
        download_text = findViewById(R.id.download_text);
        mediaRouteButton = findViewById(R.id.media_route_button);
        chromeCastTv = findViewById(R.id.chrome_cast_tv);
        castControlView = findViewById(R.id.cast_control_view);
        tvLayout = findViewById(R.id.tv_layout);
        sheduleLayout = findViewById(R.id.p_shedule_layout);
        tvTitleTv = findViewById(R.id.tv_title_tv);
        programRv = findViewById(R.id.program_guide_rv);
        tvTopLayout = findViewById(R.id.tv_top_layout);
        tvThumbIv = findViewById(R.id.tv_thumb_iv);
        shareIv = findViewById(R.id.share_iv);
        watchStatusTv = findViewById(R.id.watch_status_tv);
        timeTv = findViewById(R.id.time_tv);
        programTv = findViewById(R.id.program_type_tv);
        exoRewind = findViewById(R.id.rewind_layout);
        exoForward = findViewById(R.id.forward_layout);
        seekbarLayout = findViewById(R.id.seekbar_layout);
        liveTv = findViewById(R.id.live_tv);
        castRv = findViewById(R.id.cast_rv);
        proGuideTv = findViewById(R.id.pro_guide_tv);
        watchLiveTv = findViewById(R.id.watch_live_tv);

        if (isDark) {
            tvTopLayout.setBackgroundColor(getResources().getColor(R.color.dark));
            sheduleLayout.setBackground(getResources().getDrawable(R.drawable.rounded_black_transparent));
            etComment.setBackground(getResources().getDrawable(R.drawable.rounded_black_transparent));
            btnComment.setTextColor(getResources().getColor(R.color.grey_20));
        }

        // chrome cast
        CastButtonFactory.setUpMediaRouteButton(getApplicationContext(), mediaRouteButton);
        castContext = CastContext.getSharedInstance(this);
        castPlayer = new CastPlayer(castContext);
        castPlayer.setSessionAvailabilityListener(this);

        // cast button will show if the cast device will be available
        if(castContext.getCastState() != CastState.NO_DEVICES_AVAILABLE)
            mediaRouteButton.setVisibility(View.VISIBLE);

        // start the shimmer effect
        shimmerFrameLayout.startShimmer();


        playerHeight = lPlay.getLayoutParams().height;


        progressBar.setMax(100); // 100 maximum value for the progress value
        progressBar.setProgress(50);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.setWebChromeClient(new WebChromeClient());


        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        type = getIntent().getStringExtra("vType");
        id = getIntent().getStringExtra("id");
        castSession = getIntent().getBooleanExtra("castSession", false);


        // getting user login info for favourite button visibility
        final SharedPreferences preferences = getSharedPreferences("user", MODE_PRIVATE);
        if (preferences.getBoolean("status", false)) {
            imgAddFav.setVisibility(VISIBLE);
        } else {
            imgAddFav.setVisibility(GONE);
        }


        commentsAdapter = new CommentsAdapter(this, listComment);
        rvComment.setLayoutManager(new LinearLayoutManager(this));
        rvComment.setHasFixedSize(true);
        rvComment.setNestedScrollingEnabled(false);
        rvComment.setAdapter(commentsAdapter);

        commentURl = new ApiResources().getCommentsURL().concat("&&id=").concat(id);

        getComments(commentURl);


        imgFull.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                controlFullScreenPlayer();

            }
        });


        imgSubtitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                showDialog(DetailsActivity.this, listSub);

            }
        });


        btnComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!preferences.getBoolean("status", false)) {
                    startActivity(new Intent(DetailsActivity.this, LoginActivity.class));
                    new ToastMsg(DetailsActivity.this).toastIconError(getString(R.string.login_first));
                } else if (etComment.getText().toString().equals("")) {

                    new ToastMsg(DetailsActivity.this).toastIconError(getString(R.string.comment_empty));

                } else {

                    String commentUrl = new ApiResources().getAddComment()
                            .concat("&&videos_id=")
                            .concat(id).concat("&&user_id=")
                            .concat(preferences.getString("id", "0"))
                            .concat("&&comment=").concat(etComment.getText().toString());

                    commentUrl = commentUrl.replaceAll(" ", "%20");
                    commentUrl = commentUrl.replaceAll("\n", "%0A");
                    addComment(commentUrl);

                }

            }
        });

        imgAddFav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                String url = new ApiResources().getAddFav() + "&&user_id=" + preferences.getString("id", "0") + "&&videos_id=" + id;

                if (isFav) {
                    String removeURL = new ApiResources().getRemoveFav() + "&&user_id=" + preferences.getString("id", "0") + "&&videos_id=" + id;
                    removeFromFav(removeURL);
                } else {
                    addToFav(url);
                }
            }
        });


        if (!isNetworkAvailable()) {
            new ToastMsg(DetailsActivity.this).toastIconError(getString(R.string.no_internet));
        }


        initGetData();
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                clear_previous();
                initGetData();
            }
        });

        loadAd();

    }

    public void controlFullScreenPlayer() {
        if (isFullScr) {
            fullScreenByClick = false;
            isFullScr = false;
            swipeRefreshLayout.setVisibility(VISIBLE);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

            if (isVideo) {
                lPlay.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, playerHeight));

            } else {
                lPlay.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, playerHeight));
            }

            // reset the orientation
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

        } else {
            fullScreenByClick = true;
            isFullScr = true;
            swipeRefreshLayout.setVisibility(GONE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

            if (isVideo) {
                lPlay.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));

            } else {
                lPlay.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));

            }

            // reset the orientation
            //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

        }
    }



    @Override
    protected void onStart() {
        super.onStart();



        watchLiveTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideExoControlForTv();
                //Toast.makeText(DetailsActivity.this, "media:"+mediaUrl, Toast.LENGTH_SHORT).show();
                iniMoviePlayer(mediaUrl, serverType, DetailsActivity.this);

                watchStatusTv.setText(getString(R.string.watching_on) + " "+ getString(R.string.app_name));
                watchLiveTv.setVisibility(GONE);

                timeTv.setText(currentProgramTime);
                programTv.setText(currentProgramTitle);
            }
        });

        shareIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Tools.share(DetailsActivity.this, title);
            }
        });

        castPlayer.addListener(new Player.DefaultEventListener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

                if (playWhenReady && playbackState == CastPlayer.STATE_READY) {
                    progressBar.setVisibility(View.GONE);

                    Log.e("STATE PLAYER:::", String.valueOf(isPlaying));

                } else if (playbackState == CastPlayer.STATE_READY) {
                    progressBar.setVisibility(View.GONE);
                    Log.e("STATE PLAYER:::", String.valueOf(isPlaying));
                } else if (playbackState == CastPlayer.STATE_BUFFERING) {
                    progressBar.setVisibility(VISIBLE);

                    Log.e("STATE PLAYER:::", String.valueOf(isPlaying));
                } else {
                    Log.e("STATE PLAYER:::", String.valueOf(isPlaying));
                }

            }
        });

        downloadIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaUrl != null) {
                    //Toast.makeText(DetailsActivity.this, "Downloading ...", Toast.LENGTH_SHORT).show();
                    downloadVideo(mediaUrl);
                } else {
                    Toast.makeText(DetailsActivity.this, "null media", Toast.LENGTH_SHORT).show();
                }
                //Toast.makeText(DetailsActivity.this, "clicked", Toast.LENGTH_SHORT).show();

            }
        });


        simpleExoPlayerView.setControllerVisibilityListener(new PlayerControlView.VisibilityListener() {
            @Override
            public void onVisibilityChange(int visibility) {
                Log.e("Visibil", String.valueOf(visibility));
                if (visibility == 0) {
                    imgBack.setVisibility(VISIBLE);
                    imgFull.setVisibility(VISIBLE);

                    // invisible download icon for live tv
                    if (download_check.equals("1")) {
                        if (!tv) {
                            downloadIv.setVisibility(VISIBLE);
                        } else {
                            downloadIv.setVisibility(GONE);
                        }
                    } else {
                        downloadIv.setVisibility(GONE);
                    }

                    if (listSub.size() != 0) {
                        imgSubtitle.setVisibility(VISIBLE);
                    }
                    //imgSubtitle.setVisibility(VISIBLE);
                } else {
                    imgBack.setVisibility(GONE);
                    imgFull.setVisibility(GONE);
                    imgSubtitle.setVisibility(GONE);
                    downloadIv.setVisibility(GONE);
                }
            }
        });


    }


    void clear_previous() {
        strCast = "";
        strDirector = "";
        strGenre = "";
        listDownload.clear();
        programs.clear();
        castCrews.clear();
    }


    public void showDialog(Context context, List<SubtitleModel> list) {


        ViewGroup viewGroup = findViewById(android.R.id.content);

        View dialogView = LayoutInflater.from(this).inflate(R.layout.custom_dialog_subtitle, viewGroup, false);

        ImageView cancel = dialogView.findViewById(R.id.cancel);


        RecyclerView recyclerView = dialogView.findViewById(R.id.recyclerView);
        SubtitleAdapter adapter = new SubtitleAdapter(context, list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);

        alertDialog = builder.create();
        alertDialog.show();

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.cancel();
            }
        });

    }

    @Override
    public void onCastSessionAvailable() {
        castSession = true;

        

        MediaMetadata movieMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);
        movieMetadata.putString(MediaMetadata.KEY_TITLE, title);
        //movieMetadata.putString(MediaMetadata.KEY_ALBUM_ARTIST, "Test Artist");
        movieMetadata.addImage(new WebImage(Uri.parse(castImageUrl)));
        MediaInfo mediaInfo = new MediaInfo.Builder(mediaUrl)
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setContentType(MimeTypes.VIDEO_UNKNOWN)
                .setMetadata(movieMetadata).build();

        //array of media sources
        final MediaQueueItem[] mediaItems = {new MediaQueueItem.Builder(mediaInfo).build()};

        castPlayer.loadItems(mediaItems, 0, 3000, Player.REPEAT_MODE_OFF);

        // visible control ui of casting
        castControlView.setVisibility(VISIBLE);
        castControlView.setPlayer(castPlayer);
        castControlView.setVisibilityListener(new PlaybackControlView.VisibilityListener() {
            @Override
            public void onVisibilityChange(int visibility) {
                if (visibility == GONE) {
                    castControlView.setVisibility(VISIBLE);
                    chromeCastTv.setVisibility(VISIBLE);
                }
            }
        });

        // invisible control ui of exoplayer
        player.setPlayWhenReady(false);
        simpleExoPlayerView.setUseController(false);
    }

    @Override
    public void onCastSessionUnavailable() {
// make cast session false
        castSession = false;


        // invisible control ui of exoplayer
        player.setPlayWhenReady(true);
        simpleExoPlayerView.setUseController(true);

        // invisible control ui of casting
        castControlView.setVisibility(GONE);
        chromeCastTv.setVisibility(GONE);
    }

    public void initServerTypeForTv(String serverType) {
        this.serverType = serverType;
    }

    @Override
    public void onProgramClick(Program program) {
        if (program.getProgramStatus().equals("onaired")) {
            showExoControlForTv();
            iniMoviePlayer(program.getVideoUrl(), "tv",this);
            timeTv.setText(program.getTime());
            programTv.setText(program.getTitle());
        } else {
            new ToastMsg(DetailsActivity.this).toastIconError("Not Yet");
        }
    }

    private class SubtitleAdapter extends RecyclerView.Adapter<SubtitleAdapter.OriginalViewHolder> {

        private List<SubtitleModel> items = new ArrayList<>();
        private Context ctx;

        public SubtitleAdapter(Context context, List<SubtitleModel> items) {
            this.items = items;
            ctx = context;
        }


        @Override
        public SubtitleAdapter.OriginalViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            SubtitleAdapter.OriginalViewHolder vh;
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_subtitle, parent, false);
            vh = new SubtitleAdapter.OriginalViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(SubtitleAdapter.OriginalViewHolder holder, final int position) {

            final SubtitleModel obj = items.get(position);
            holder.name.setText(obj.getLang());

            holder.lyt_parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    setSelectedSubtitle(mediaSource, obj.getUrl(), ctx);
                    alertDialog.cancel();

                }
            });

        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        public class OriginalViewHolder extends RecyclerView.ViewHolder {

            public TextView name;
            private View lyt_parent;


            public OriginalViewHolder(View v) {
                super(v);
                name = v.findViewById(R.id.name);
                lyt_parent = v.findViewById(R.id.lyt_parent);
            }
        }


    }


    private void loadAd() {


        if (ApiResources.adStatus.equals("1")) {

            BannerAds.ShowBannerAds(this, adView);
            PopUpAds.ShowInterstitialAds(this);

            mInterstitialAd = new InterstitialAd(this);
            mInterstitialAd.setAdUnitId(ApiResources.adMobInterstitialId);
            mInterstitialAd.loadAd(new AdRequest.Builder().build());


            mInterstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();

                    Random rand = new Random();
                    int i = rand.nextInt(10) + 1;

                    if (i % 2 == 0) {
                        mInterstitialAd.show();
                    }
                }

                @Override
                public void onAdFailedToLoad(int i) {
                    super.onAdFailedToLoad(i);

                }
            });

        }
    }

    private void initGetData() {

        if (!type.equals("tv")) {


            //----related rv----------
            relatedAdapter = new HomePageAdapter(this, listRelated);
            rvRelated.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,
                    false));
            rvRelated.setHasFixedSize(true);
            rvRelated.setAdapter(relatedAdapter);

            if (type.equals("tvseries")) {

                rvRelated.removeAllViews();
                listRelated.clear();
                rvServer.removeAllViews();
                listDirector.clear();
                listEpisode.clear();

                downloadIv.setVisibility(VISIBLE);

                episodeAdapter = new EpisodeAdapter(this, listDirector, isDark);
                rvServer.setLayoutManager(new LinearLayoutManager(this));
                rvServer.setHasFixedSize(true);
                rvServer.setAdapter(episodeAdapter);

                // cast & crew adapter
                castCrewAdapter = new CastCrewAdapter(this, castCrews);
                castRv.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
                castRv.setHasFixedSize(true);
                castRv.setAdapter(castCrewAdapter);


                getSeriesData(type, id);

                if (listSub.size() == 0) {
                    imgSubtitle.setVisibility(GONE);
                }

            } else {
                downloadIv.setVisibility(VISIBLE);
                rvServer.removeAllViews();
                listDirector.clear();
                rvRelated.removeAllViews();
                listRelated.clear();
                if (listSub.size() == 0) {
                    imgSubtitle.setVisibility(GONE);
                }

                //---server adapter----
                serverAdapter = new ServerApater(this, listDirector);
                rvServer.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
                rvServer.setHasFixedSize(true);
                rvServer.setAdapter(serverAdapter);

                //---download adapter--------
                downloadAdapter = new DownloadAdapter(this, listDownload);
                rvDownload.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
                rvDownload.setHasFixedSize(true);
                rvDownload.setAdapter(downloadAdapter);

                // cast & crew adapter
                castCrewAdapter = new CastCrewAdapter(this, castCrews);
                castRv.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
                castRv.setHasFixedSize(true);
                castRv.setAdapter(castCrewAdapter);

                getData(type, id);

                final ServerApater.OriginalViewHolder[] viewHolder = {null};
                serverAdapter.setOnItemClickListener(new ServerApater.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, CommonModels obj, int position, ServerApater.OriginalViewHolder holder) {
                        mediaUrl = obj.getStremURL();

                        if (!castSession) {
                            iniMoviePlayer(obj.getStremURL(), obj.getServerType(), DetailsActivity.this);
                            listSub.clear();
                            listSub.addAll(obj.getListSub());

                            if (listSub.size() != 0) {
                                imgSubtitle.setVisibility(VISIBLE);
                            }

                        } else {
                            if (obj.getServerType().toLowerCase().equals("embed")) {

                                castSession = false;
                                castPlayer.setSessionAvailabilityListener(null);
                                castPlayer.release();

                                // invisible control ui of exoplayer
                                player.setPlayWhenReady(true);
                                simpleExoPlayerView.setUseController(true);

                                // invisible control ui of casting
                                castControlView.setVisibility(GONE);
                                chromeCastTv.setVisibility(GONE);


                            } else {
                                showQueuePopup(DetailsActivity.this, null, getMediaInfo());
                            }
                        }

                        serverAdapter.chanColor(viewHolder[0], position);
                        holder.name.setTextColor(getResources().getColor(R.color.colorPrimary));
                        viewHolder[0] = holder;


                    }

                    @Override
                    public void getFirstUrl(String url) {
                        mediaUrl = url;
                    }
                });
            }

            SharedPreferences sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);
            String url = new ApiResources().getFavStatusURl() + "&&user_id=" + sharedPreferences.getString("id", "0") + "&&videos_id=" + id;

            if (sharedPreferences.getBoolean("status", false)) {
                getFavStatus(url);
            }

        } else {

            tv = true;
            downloadIv.setVisibility(GONE);
            imgSubtitle.setVisibility(GONE);
            llcomment.setVisibility(GONE);

            // hide exo player some control
            hideExoControlForTv();

            tvLayout.setVisibility(VISIBLE);

            // hide program guide if its disable from api
            if (!Constants.IS_ENABLE_PROGRAM_GUIDE) {
                proGuideTv.setVisibility(GONE);
                programRv.setVisibility(GONE);

            }

            watchStatusTv.setText(getString(R.string.watching_on)+" "+ getString(R.string.app_name));

            tvRelated.setText(getString(R.string.all_tv_channel));

            rvServer.removeAllViews();
            listDirector.clear();
            rvRelated.removeAllViews();
            listRelated.clear();


            programAdapter = new ProgramAdapter(programs, this);
            programAdapter.setOnProgramClickListener(this);
            programRv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            programRv.setHasFixedSize(true);
            programRv.setAdapter(programAdapter);

            //----related rv----------
            relatedTvAdapter = new LiveTvHomeAdapter(this, listRelated, TAG);
            rvRelated.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            rvRelated.setHasFixedSize(true);
            rvRelated.setAdapter(relatedTvAdapter);



            imgAddFav.setVisibility(GONE);


            serverAdapter = new ServerApater(this, listDirector);
            rvServer.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            rvServer.setHasFixedSize(true);
            rvServer.setAdapter(serverAdapter);
            getTvData(type, id);
            llBottom.setVisibility(GONE);

            final ServerApater.OriginalViewHolder[] viewHolder = {null};
            serverAdapter.setOnItemClickListener(new ServerApater.OnItemClickListener() {
                @Override
                public void onItemClick(View view, CommonModels obj, int position, ServerApater.OriginalViewHolder holder) {
                    mediaUrl = obj.getStremURL();

                    if (!castSession) {
                        iniMoviePlayer(obj.getStremURL(), obj.getServerType(), DetailsActivity.this);

                    } else {

                        if (obj.getServerType().toLowerCase().equals("embed")) {

                            castSession = false;
                            castPlayer.setSessionAvailabilityListener(null);
                            castPlayer.release();

                            // invisible control ui of exoplayer
                            player.setPlayWhenReady(true);
                            simpleExoPlayerView.setUseController(true);

                            // invisible control ui of casting
                            castControlView.setVisibility(GONE);
                            chromeCastTv.setVisibility(GONE);


                        } else {
                            showQueuePopup(DetailsActivity.this, null, getMediaInfo());
                        }
                    }

                    serverAdapter.chanColor(viewHolder[0], position);
                    holder.name.setTextColor(getResources().getColor(R.color.colorPrimary));
                    viewHolder[0] = holder;


                }

                @Override
                public void getFirstUrl(String url) {
                    mediaUrl = url;
                }
            });


        }
    }


    private void openWebActivity(String s, Context context, String type) {

        if (isPlaying) {
            player.release();

        }

        progressBar.setVisibility(GONE);
        playerLayout.setVisibility(GONE);
        downloadIv.setVisibility(GONE);

        webView.loadUrl(s);
        webView.setWebChromeClient(new WebChromeClient());
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.setVisibility(VISIBLE);

        // open to webview activity
        /*Intent intent = new Intent(this, WebViewActivity.class);
        intent.putExtra("url", s);
        startActivity(intent);*/

    }


    public void iniMoviePlayer(String url, String type, Context context) {

        Log.e("vTYpe :: ", type);


        if (type.equals("embed") || type.equals("vimeo") || type.equals("gdrive") || type.equals("youtube-live")) {
            isVideo = false;
            openWebActivity(url, context, type);
        } else {
            isVideo = true;
            initVideoPlayer(url, context, type);
        }
    }


    public void initVideoPlayer(String url, Context context, String type) {

        progressBar.setVisibility(VISIBLE);

        if (player != null) {
            player.release();
        }

        webView.setVisibility(GONE);
        playerLayout.setVisibility(VISIBLE);
        if (download_check != null) {
            if (download_check.equals(0)) {
                downloadIv.setVisibility(GONE);
            } else {
                downloadIv.setVisibility(VISIBLE);
            }
        }
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory = new
                AdaptiveTrackSelection.Factory(bandwidthMeter);


        TrackSelector trackSelector = new
                DefaultTrackSelector(videoTrackSelectionFactory);

        player = ExoPlayerFactory.newSimpleInstance(context, trackSelector);
        player.setPlayWhenReady(true);
        simpleExoPlayerView.setPlayer(player);


        Uri uri = Uri.parse(url);

        if (type.equals("hls")) {
            mediaSource = hlsMediaSource(uri, context);


        } else if (type.equals("youtube")) {
            Log.e("youtube url  :: ", url);
            extractYoutubeUrl(url, context, 18);
        } else if (type.equals("youtube-live")) {
            Log.e("youtube url  :: ", url);
            extractYoutubeUrl(url, context, 133);
        } else if (type.equals("rtmp")) {
            mediaSource = rtmpMediaSource(uri);
        } else {
            mediaSource = mediaSource(uri, context);
        }

        //Toast.makeText(context, "castSession:"+getCastSessionObj()+"", Toast.LENGTH_SHORT).show();
        player.prepare(mediaSource, true, false);

        player.addListener(new Player.DefaultEventListener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

                if (playWhenReady && playbackState == Player.STATE_READY) {

                    isPlaying = true;
                    progressBar.setVisibility(View.GONE);

                    Log.e("STATE PLAYER:::", String.valueOf(isPlaying));

                } else if (playbackState == Player.STATE_READY) {
                    progressBar.setVisibility(View.GONE);
                    isPlaying = false;
                    Log.e("STATE PLAYER:::", String.valueOf(isPlaying));
                } else if (playbackState == Player.STATE_BUFFERING) {
                    isPlaying = false;
                    progressBar.setVisibility(VISIBLE);

                    Log.e("STATE PLAYER:::", String.valueOf(isPlaying));
                } else {
                    // player paused in any state
                    isPlaying = false;
                    Log.e("STATE PLAYER:::", String.valueOf(isPlaying));
                }

            }
        });
    }


    @SuppressLint("StaticFieldLeak")
    private void extractYoutubeUrl(String url, final Context context, final int tag) {


        new YouTubeExtractor(context) {
            @Override
            public void onExtractionComplete(SparseArray<YtFile> ytFiles, VideoMeta vMeta) {
                if (ytFiles != null) {
                    int itag = tag;
                    String downloadUrl = ytFiles.get(itag).getUrl();
                    Log.e("YOUTUBE::", String.valueOf(downloadUrl));

                    player.setPlayWhenReady(false);

                    try {

                        MediaSource mediaSource = mediaSource(Uri.parse(downloadUrl), context);
                        player.prepare(mediaSource, true, false);


                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                }
            }
        }.extract(url, true, true);


    }


    private MediaSource rtmpMediaSource(Uri uri) {
        MediaSource videoSource = null;


        RtmpDataSourceFactory dataSourceFactory = new RtmpDataSourceFactory();
        videoSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                .createMediaSource(uri);


        return videoSource;

    }


    private MediaSource hlsMediaSource(Uri uri, Context context) {


        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(context,
                Util.getUserAgent(context, "oxoo"), bandwidthMeter);

        MediaSource videoSource = new HlsMediaSource.Factory(dataSourceFactory)
                .createMediaSource(uri);


        return videoSource;


    }


    private MediaSource mediaSource(Uri uri, Context context) {
        return new ExtractorMediaSource.Factory(
                new DefaultHttpDataSourceFactory("exoplayer")).
                createMediaSource(uri);

    }

    public void setSelectedSubtitle(MediaSource mediaSource, String subtitle, Context context) {
        MergingMediaSource mergedSource;
        if (subtitle != null) {
            Uri subtitleUri = Uri.parse(subtitle);

            Format subtitleFormat = Format.createTextSampleFormat(
                    null, // An identifier for the track. May be null.
                    MimeTypes.TEXT_VTT, // The mime type. Must be set correctly.
                    Format.NO_VALUE, // Selection flags for the track.
                    "en"); // The subtitle language. May be null.

            DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(context,
                    Util.getUserAgent(context, CLASS_NAME), new DefaultBandwidthMeter());


            MediaSource subtitleSource = new SingleSampleMediaSource
                    .Factory(dataSourceFactory)
                    .createMediaSource(subtitleUri, subtitleFormat, C.TIME_UNSET);


            mergedSource = new MergingMediaSource(mediaSource, subtitleSource);
            player.prepare(mergedSource, false, false);
            //resumePlayer();

        } else {
            Toast.makeText(context, "there is no subtitle", Toast.LENGTH_SHORT).show();
        }
    }


    private void addToFav(String url) {

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                try {

                    if (response.getString("status").equals("success")) {
                        new ToastMsg(DetailsActivity.this).toastIconSuccess(response.getString("message"));
                        isFav = true;
                        imgAddFav.setBackgroundResource(R.drawable.outline_favorite_24);
                    } else {
                        new ToastMsg(DetailsActivity.this).toastIconError(response.getString("message"));
                    }

                } catch (Exception e) {

                } finally {

                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                new ToastMsg(DetailsActivity.this).toastIconError(getString(R.string.error_toast));
            }
        });
        new VolleySingleton(DetailsActivity.this).addToRequestQueue(jsonObjectRequest);


    }

    private void getTvData(String vtype, String vId) {


        String type = "&&type=" + vtype;
        String id = "&id=" + vId;
        String url = new ApiResources().getDetails() + type + id;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                swipeRefreshLayout.setRefreshing(false);
                shimmerFrameLayout.stopShimmer();
                shimmerFrameLayout.setVisibility(GONE);

                try {
                    title = response.getString("tv_name");
                    tvName.setText(title);
                    tvName.setVisibility(GONE);
                    tvTitleTv.setText(title);

                    tvDes.setText(response.getString("description"));
                    V_URL = response.getString("stream_url");
                    castImageUrl = response.getString("thumbnail_url");

                    Picasso.get().load(response.getString("thumbnail_url")).placeholder(R.drawable.album_art_placeholder)
                            .into(tvThumbIv);


                    CommonModels model = new CommonModels();
                    model.setTitle("HD");
                    model.setStremURL(V_URL);
                    model.setServerType(response.getString("stream_from"));
                    listDirector.add(model);


                    currentProgramTime = response.getString("current_program_time");
                    currentProgramTitle = response.getString("current_program_title");

                    timeTv.setText(currentProgramTime);
                    programTv.setText(currentProgramTitle);

                    if (Constants.IS_ENABLE_PROGRAM_GUIDE) {
                        JSONArray programGuideArr = response.getJSONArray("program_guide");
                        //Toast.makeText(DetailsActivity.this, "p"+programGuideArr.length(), Toast.LENGTH_SHORT).show();
                        for (int i = 0; i < programGuideArr.length(); i++) {

                            JSONObject jsonObject = programGuideArr.getJSONObject(i);
                            Program program = new Program();

                            program.setId(jsonObject.getString("id"));
                            program.setTitle(jsonObject.getString("title"));
                            program.setProgramStatus(jsonObject.getString("program_status"));
                            program.setTime(jsonObject.getString("time"));
                            program.setVideoUrl(jsonObject.getString("video_url"));

                            programs.add(program);
                        }
                        if (programs.size() <= 0) {
                            proGuideTv.setVisibility(GONE);
                            programRv.setVisibility(GONE);
                        } else {
                            proGuideTv.setVisibility(VISIBLE);
                            programRv.setVisibility(VISIBLE);
                            programAdapter.notifyDataSetChanged();
                        }
                    }


                    JSONArray jsonArray = response.getJSONArray("all_tv_channel");
                    for (int i = 0; i < jsonArray.length(); i++) {

                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        CommonModels models = new CommonModels();
                        models.setImageUrl(jsonObject.getString("poster_url"));
                        models.setTitle(jsonObject.getString("tv_name"));
                        models.setVideoType("tv");
                        models.setId(jsonObject.getString("live_tv_id"));
                        listRelated.add(models);

                    }
                    if (listRelated.size() == 0) {
                        tvRelated.setVisibility(GONE);
                    }
                    relatedTvAdapter.notifyDataSetChanged();


                    JSONArray serverArray = response.getJSONArray("additional_media_source");
                    for (int i = 0; i < serverArray.length(); i++) {
                        JSONObject jsonObject = serverArray.getJSONObject(i);

                        CommonModels models = new CommonModels();
                        models.setTitle(jsonObject.getString("label"));
                        models.setStremURL(jsonObject.getString("url"));
                        models.setServerType(jsonObject.getString("source"));


                        listDirector.add(models);
                    }
                    serverAdapter.notifyDataSetChanged();


                } catch (Exception e) {

                } finally {

                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        new VolleySingleton(DetailsActivity.this).addToRequestQueue(jsonObjectRequest);

    }

    private void getSeriesData(String vtype, String vId) {



        String type = "&&type=" + vtype;
        String id = "&id=" + vId;
        String url = new ApiResources().getDetails() + type + id;


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                swipeRefreshLayout.setRefreshing(false);
                shimmerFrameLayout.stopShimmer();
                shimmerFrameLayout.setVisibility(GONE);
                try {
                    title = response.getString("title");
                    castImageUrl = response.getString("thumbnail_url");

                    seriesTitle = title;

                    tvName.setText(title);
                    tvRelease.setText("Release On " + response.getString("release"));
                    tvDes.setText(response.getString("description"));

                    download_check = response.getString("enable_download");
                    if (download_check.equals("1")) {
                        downloadIv.setVisibility(VISIBLE);
                    } else {
                        downloadIv.setVisibility(GONE);
                    }

                    //----director---------------
                    JSONArray directorArray = response.getJSONArray("director");
                    for (int i = 0; i < directorArray.length(); i++) {
                        JSONObject jsonObject = directorArray.getJSONObject(i);
                        if (i == directorArray.length() - 1) {
                            strDirector = strDirector + jsonObject.getString("name");
                        } else {
                            strDirector = strDirector + jsonObject.getString("name") + ",";
                        }
                    }
                    tvDirector.setText(strDirector);


                    //----cast---------------
                    JSONArray castArray = response.getJSONArray("cast");
                    for (int i = 0; i < castArray.length(); i++) {
                        JSONObject jsonObject = castArray.getJSONObject(i);

                        CastCrew castCrew = new CastCrew();
                        castCrew.setId(jsonObject.getString("star_id"));
                        castCrew.setName(jsonObject.getString("name"));
                        castCrew.setUrl(jsonObject.getString("url"));
                        castCrew.setImageUrl(jsonObject.getString("image_url"));
                        castCrews.add(castCrew);

                    }
                    castCrewAdapter.notifyDataSetChanged();


                    //---genre---------------
                    JSONArray genreArray = response.getJSONArray("genre");
                    for (int i = 0; i < genreArray.length(); i++) {
                        JSONObject jsonObject = genreArray.getJSONObject(i);
                        if (i == castArray.length() - 1) {
                            strGenre = strGenre + jsonObject.getString("name");
                        } else {
                            if (i == genreArray.length()-1) {
                                strGenre = strGenre + jsonObject.getString("name");
                            } else {
                                strGenre = strGenre + jsonObject.getString("name") + ",";
                            }
                        }
                    }
                    tvGenre.setText(strGenre);

                    //----realted post---------------
                    JSONArray relatedArray = response.getJSONArray("related_tvseries");
                    for (int i = 0; i < relatedArray.length(); i++) {
                        JSONObject jsonObject = relatedArray.getJSONObject(i);

                        CommonModels models = new CommonModels();
                        models.setTitle(jsonObject.getString("title"));
                        models.setImageUrl(jsonObject.getString("thumbnail_url"));
                        models.setId(jsonObject.getString("videos_id"));
                        models.setVideoType("tvseries");

                        listRelated.add(models);
                    }
                    if (listRelated.size() == 0) {
                        tvRelated.setVisibility(GONE);
                    }
                    relatedAdapter.notifyDataSetChanged();


                    //----episode------------
                    JSONArray mainArray = response.getJSONArray("season");


                    for (int i = 0; i < mainArray.length(); i++) {
                        //epList.clear();

                        JSONObject jsonObject = mainArray.getJSONObject(i);

                        CommonModels models = new CommonModels();
                        String season_name = jsonObject.getString("seasons_name");
                        models.setTitle(jsonObject.getString("seasons_name"));


                        Log.e("Season Name 1::", jsonObject.getString("seasons_name"));

                        JSONArray episodeArray = jsonObject.getJSONArray("episodes");
                        List<EpiModel> epList = new ArrayList<>();
                        epList.clear();
                        for (int j = 0; j < episodeArray.length(); j++) {

                            JSONObject object = episodeArray.getJSONObject(j);

                            EpiModel model = new EpiModel();
                            model.setSeson(season_name);
                            model.setEpi(object.getString("episodes_name"));
                            model.setStreamURL(object.getString("file_url"));
                            model.setServerType(object.getString("file_type"));
                            model.setImageUrl(object.getString("image_url"));
                            epList.add(model);
                        }
                        models.setListEpi(epList);
                        listDirector.add(models);

                        episodeAdapter = new EpisodeAdapter(DetailsActivity.this, listDirector, isDark);
                        rvServer.setAdapter(episodeAdapter);
                        episodeAdapter.notifyDataSetChanged();

                    }


                } catch (Exception e) {

                } finally {

                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        new VolleySingleton(DetailsActivity.this).addToRequestQueue(jsonObjectRequest);


    }


    private void getData(String vtype, String vId) {


        String type = "&&type=" + vtype;
        String id = "&id=" + vId;

        strCast = "";
        strDirector = "";
        strGenre = "";


        String url = new ApiResources().getDetails() + type + id;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                shimmerFrameLayout.stopShimmer();
                shimmerFrameLayout.setVisibility(GONE);
                swipeRefreshLayout.setRefreshing(false);
                try {

                    download_check = response.getString("enable_download");
                    castImageUrl = response.getString("thumbnail_url");
                    if (download_check.equals("1")) {
                        //download_text.setVisibility(VISIBLE);
                        downloadIv.setVisibility(VISIBLE);
                        download_text.setVisibility(VISIBLE);
                        rvDownload.setVisibility(VISIBLE);
                    } else {
                        downloadIv.setVisibility(GONE);
                        download_text.setVisibility(GONE);
                        rvDownload.setVisibility(GONE);
                    }
                    String title = response.getString("title");
                    movieTitle = title;

                    tvName.setText(title);
                    tvRelease.setText("Release On " + response.getString("release"));
                    tvDes.setText(response.getString("description"));

                    //----director---------------
                    JSONArray directorArray = response.getJSONArray("director");
                    for (int i = 0; i < directorArray.length(); i++) {
                        JSONObject jsonObject = directorArray.getJSONObject(i);
                        if (i == directorArray.length() - 1) {
                            strDirector = strDirector + jsonObject.getString("name");
                        } else {
                            strDirector = strDirector + jsonObject.getString("name") + ",";
                        }
                    }

                    tvDirector.setText(strDirector);

                    //----cast---------------
                    JSONArray castArray = response.getJSONArray("cast");
                    for (int i = 0; i < castArray.length(); i++) {
                        JSONObject jsonObject = castArray.getJSONObject(i);

                        CastCrew castCrew = new CastCrew();
                        castCrew.setId(jsonObject.getString("star_id"));
                        castCrew.setName(jsonObject.getString("name"));
                        castCrew.setUrl(jsonObject.getString("url"));
                        castCrew.setImageUrl(jsonObject.getString("image_url"));

                        castCrews.add(castCrew);

                    }
                    castCrewAdapter.notifyDataSetChanged();


                    //---genre---------------
                    JSONArray genreArray = response.getJSONArray("genre");
                    for (int i = 0; i < genreArray.length(); i++) {
                        JSONObject jsonObject = genreArray.getJSONObject(i);
                        if (i == castArray.length() - 1) {
                            strGenre = strGenre + jsonObject.getString("name");
                        } else {
                            if (i == genreArray.length()-1) {
                                strGenre = strGenre + jsonObject.getString("name");
                            } else {
                                strGenre = strGenre + jsonObject.getString("name") + ",";
                            }
                        }
                    }
                    tvGenre.setText(strGenre);

                    //----server---------------
                    JSONArray serverArray = response.getJSONArray("videos");
                    for (int i = 0; i < serverArray.length(); i++) {
                        JSONObject jsonObject = serverArray.getJSONObject(i);

                        CommonModels models = new CommonModels();
                        models.setTitle(jsonObject.getString("label"));
                        models.setStremURL(jsonObject.getString("file_url"));
                        models.setServerType(jsonObject.getString("file_type"));


                        if (jsonObject.getString("file_type").equals("mp4")) {
                            V_URL = jsonObject.getString("file_url");
                        }

                        //----subtitle-----------
                        JSONArray subArray = jsonObject.getJSONArray("subtitle");

                        if (subArray.length() != 0) {

                            List<SubtitleModel> list = new ArrayList<>();

                            for (int j = 0; j < subArray.length(); j++) {
                                JSONObject subObject = subArray.getJSONObject(j);

                                SubtitleModel subtitleModel = new SubtitleModel();

                                //strSubtitle = subObject.getString("url");
                                subtitleModel.setUrl(subObject.getString("url"));
                                subtitleModel.setLang(subObject.getString("language"));

                                list.add(subtitleModel);
                            }
                            if (i == 0) {
                                listSub.addAll(list);
                            }

                            models.setListSub(list);


                        } else {
                            models.setSubtitleURL(strSubtitle);
                        }

                        //models.setSubtitleURL("null");

                        listDirector.add(models);
                    }
                    serverAdapter.notifyDataSetChanged();


                    //----related post---------------
                    JSONArray relatedArray = response.getJSONArray("related_movie");
                    for (int i = 0; i < relatedArray.length(); i++) {
                        JSONObject jsonObject = relatedArray.getJSONObject(i);
                        //Toast.makeText(DetailsActivity.this, "sadfjhi"+ jsonObject.toString(), Toast.LENGTH_SHORT).show();
                        CommonModels models = new CommonModels();
                        models.setTitle(jsonObject.getString("title"));
                        models.setImageUrl(jsonObject.getString("thumbnail_url"));
                        models.setId(jsonObject.getString("videos_id"));
                        models.setVideoType("movie");

                        listRelated.add(models);
                    }
                    if (listRelated.size() == 0) {
                        tvRelated.setVisibility(GONE);
                    }
                    relatedAdapter.notifyDataSetChanged();

                    //----download list---------
                    JSONArray downloadArray = response.getJSONArray("download_links");
                    for (int i = 0; i < downloadArray.length(); i++) {
                        JSONObject jsonObject = downloadArray.getJSONObject(i);

                        CommonModels models = new CommonModels();
                        models.setTitle(jsonObject.getString("label"));
                        models.setStremURL(jsonObject.getString("download_url"));
                        models.setFileSize(jsonObject.getString("file_size"));
                        models.setResulation(jsonObject.getString("resolution"));
                        listDownload.add(models);
                    }

                    //Toast.makeText(DetailsActivity.this, "download size:"+listDownload.size(), Toast.LENGTH_SHORT).show();
                    downloadAdapter.notifyDataSetChanged();

                } catch (Exception e) {
                    e.printStackTrace();

                } finally {

                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        new VolleySingleton(DetailsActivity.this).addToRequestQueue(jsonObjectRequest);


    }


    private void getFavStatus(String url) {

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                try {

                    if (response.getString("status").equals("success")) {
                        isFav = true;
                        imgAddFav.setBackgroundResource(R.drawable.outline_favorite_24);
                        imgAddFav.setVisibility(VISIBLE);
                    } else {
                        isFav = false;
                        imgAddFav.setBackgroundResource(R.drawable.outline_favorite_border_24);
                        imgAddFav.setVisibility(VISIBLE);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
        new VolleySingleton(DetailsActivity.this).addToRequestQueue(jsonObjectRequest);

    }

    private void removeFromFav(String url) {

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                try {

                    if (response.getString("status").equals("success")) {
                        isFav = false;
                        new ToastMsg(DetailsActivity.this).toastIconSuccess(response.getString("message"));
                        imgAddFav.setBackgroundResource(R.drawable.outline_favorite_border_24);
                    } else {
                        isFav = true;
                        new ToastMsg(DetailsActivity.this).toastIconError(response.getString("message"));
                        imgAddFav.setBackgroundResource(R.drawable.outline_favorite_24);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                new ToastMsg(DetailsActivity.this).toastIconError(getString(R.string.fetch_error));
            }
        });

        new VolleySingleton(DetailsActivity.this).addToRequestQueue(jsonObjectRequest);

    }


    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void addComment(String url) {


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                try {
                    if (response.getString("status").equals("success")) {

                        rvComment.removeAllViews();
                        listComment.clear();
                        getComments(commentURl);
                        etComment.setText("");

                    } else {
                        new ToastMsg(DetailsActivity.this).toastIconError(response.getString("message"));
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                new ToastMsg(DetailsActivity.this).toastIconError("can't comment now ! try later");
            }
        });

        VolleySingleton.getInstance(DetailsActivity.this).addToRequestQueue(jsonObjectRequest);

    }


    private void getComments(String url) {

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {

                for (int i = 0; i < response.length(); i++) {

                    try {

                        JSONObject jsonObject = response.getJSONObject(i);

                        CommentsModel model = new CommentsModel();

                        model.setName(jsonObject.getString("user_name"));
                        model.setImage(jsonObject.getString("user_img_url"));
                        model.setComment(jsonObject.getString("comments"));
                        model.setId(jsonObject.getString("comments_id"));

                        listComment.add(model);

                        commentsAdapter.notifyDataSetChanged();


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        VolleySingleton.getInstance(DetailsActivity.this).addToRequestQueue(jsonArrayRequest);

    }

    @Override
    protected void onPause() {
        super.onPause();

        Log.e("ACTIVITY:::", "PAUSE" + isPlaying);

        if (isPlaying && player != null) {

            //Log.e("PLAY:::","PAUSE");

            player.setPlayWhenReady(false);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        //castManager.removeProgressWatcher(this);

        Log.e("ACTIVITY:::", "STOP" + isPlaying);

//        if (isPlaying && player!=null){
//
//            Log.e("PLAY:::","PAUSE");
//
//            player.setPlayWhenReady(false);
//        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.e("ACTIVITY:::", "DESTROY");
        resetCastPlayer();

    }

    @Override
    public void onBackPressed() {

        // if player in full screen view it will become potrait
        if (isFullScr) {
            isFullScr = false;
            swipeRefreshLayout.setVisibility(VISIBLE);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

            if (isVideo) {
                lPlay.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, playerHeight));

            } else {
                lPlay.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, playerHeight));
            }

        } else {

            releasePlayer();
            //resetCastPlayer();
            super.onBackPressed();
        }



    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.e("ACTIVITY:::", "RESUME");
        //startPlayer();
        if (player != null) {
            Log.e("PLAY:::", "RESUME");
            player.setPlayWhenReady(true);
        }

    }

    public void releasePlayer() {

        if (player != null) {
            player.setPlayWhenReady(true);
            player.stop();
            player.release();
            player = null;
            simpleExoPlayerView.setPlayer(null);
            simpleExoPlayerView = null;
            System.out.println("releasePlayer");
        }
    }

    public void setMediaUrlForTvSeries(String url, String season, String episod) {
        mediaUrl = url;
        this.season = season;
        this.episod = episod;
    }

    public boolean getCastSession() {
        return castSession;
    }

    public void resetCastPlayer() {
        if (castPlayer != null) {
            castPlayer.setPlayWhenReady(false);
            castPlayer.release();
        }
    }

    public void showQueuePopup(final Context context, View view, final MediaInfo mediaInfo) {
        CastSession castSession =
                CastContext.getSharedInstance(context).getSessionManager().getCurrentCastSession();
        if (castSession == null || !castSession.isConnected()) {
            Log.w(TAG, "showQueuePopup(): not connected to a cast device");
            return;
        }
        final RemoteMediaClient remoteMediaClient = castSession.getRemoteMediaClient();
        if (remoteMediaClient == null) {
            Log.w(TAG, "showQueuePopup(): null RemoteMediaClient");
            return;
        }
        MediaQueueItem queueItem = new MediaQueueItem.Builder(mediaInfo).setAutoplay(
                true).setPreloadTime(PRELOAD_TIME_S).build();
        MediaQueueItem[] newItemArray = new MediaQueueItem[]{queueItem};
        remoteMediaClient.queueLoad(newItemArray, 0,
                MediaStatus.REPEAT_MODE_REPEAT_OFF, null);

        /*PopupMenu popup = new PopupMenu(context, view);
        popup.getMenuInflater().inflate(R.menu.detached_popup_add_to_queue, popup.getMenu());
        PopupMenu.OnMenuItemClickListener clickListener = new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                MediaQueueItem queueItem = new MediaQueueItem.Builder(mediaInfo).setAutoplay(
                        true).setPreloadTime(PRELOAD_TIME_S).build();
                MediaQueueItem[] newItemArray = new MediaQueueItem[]{queueItem};
                String toastMessage = null;

                if ((menuItem.getItemId() == R.id.action_play_now)) {
                    remoteMediaClient.queueLoad(newItemArray, 0,
                            MediaStatus.REPEAT_MODE_REPEAT_OFF, null);

                } else if (menuItem.getItemId() == R.id.action_play_next) {
                    remoteMediaClient.queueAppendItem(queueItem, null);
                    Toast.makeText(context, getResources().getString(R.string.queue_item_added_to_play_next),
                            Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        };
        popup.setOnMenuItemClickListener(clickListener);
        popup.show();*/
    }

    public void playNextCast(MediaInfo mediaInfo) {

        //simpleExoPlayerView.setPlayer(castPlayer);
        simpleExoPlayerView.setUseController(false);
        castControlView.setVisibility(VISIBLE);
        castControlView.setPlayer(castPlayer);
        //simpleExoPlayerView.setDefaultArtwork();
        castControlView.setVisibilityListener(new PlaybackControlView.VisibilityListener() {
            @Override
            public void onVisibilityChange(int visibility) {
                if (visibility == GONE) {
                    castControlView.setVisibility(VISIBLE);
                    chromeCastTv.setVisibility(VISIBLE);
                }
            }
        });
        CastSession castSession =
                CastContext.getSharedInstance(this).getSessionManager().getCurrentCastSession();

        if (castSession == null || !castSession.isConnected()) {
            Log.w(TAG, "showQueuePopup(): not connected to a cast device");
            return;
        }

        final RemoteMediaClient remoteMediaClient = castSession.getRemoteMediaClient();

        if (remoteMediaClient == null) {
            Log.w(TAG, "showQueuePopup(): null RemoteMediaClient");
            return;
        }
        MediaQueueItem queueItem = new MediaQueueItem.Builder(mediaInfo).setAutoplay(
                true).setPreloadTime(PRELOAD_TIME_S).build();
        MediaQueueItem[] newItemArray = new MediaQueueItem[]{queueItem};
        String toastMessage = null;

        remoteMediaClient.queueLoad(newItemArray, 0,
                MediaStatus.REPEAT_MODE_REPEAT_OFF, null);
        castPlayer.setPlayWhenReady(true);

    }

    public MediaInfo getMediaInfo() {
        MediaMetadata movieMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);
        movieMetadata.putString(MediaMetadata.KEY_TITLE, title);
        //movieMetadata.putString(MediaMetadata.KEY_ALBUM_ARTIST, "Test Artist");
        movieMetadata.addImage(new WebImage(Uri.parse(castImageUrl)));
        MediaInfo mediaInfo = new MediaInfo.Builder(mediaUrl)
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setContentType(MimeTypes.VIDEO_UNKNOWN)
                .setMetadata(movieMetadata).build();

        return mediaInfo;

    }

    public void  downloadVideo(final String url) {

        if (Build.VERSION.SDK_INT >= 23)
        {
            if (checkPermission())
            {
                // Code for above or equal 23 API Oriented Device
                // Your Permission granted already .Do next code
                Handler handler = new Handler();
                Runnable runnable = new Runnable() {
                    public void run() {
                        downloadFile(url, "");
                    }
                };
                handler.post(runnable);

            } else {
                requestPermission(); // Code for permission
            }
        }
        else
        {

            // Code for Below 23 API Oriented Device
            // Do next code

            Handler handler = new Handler();
            Runnable runnable = new Runnable() {
                public void run() {
                    downloadFile(url, "");
                }
            };
            handler.post(runnable);
        }


    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    private void requestPermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(this, "Write External Storage permission allows us to do store images. Please allow this permission in App Settings.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("value", "Permission Granted, Now you can use local drive .");
                } else {
                    Log.e("value", "Permission Denied, You cannot use local drive .");
                }
                break;
        }
    }

    public void downloadFile(String url, String filName) {

        String appName = getResources().getString(R.string.app_name);

        // creating the download directory
        File direct = new File(Environment.getExternalStorageDirectory()
                + "/"+appName+"_downloads/tv_series");
        if (!direct.exists()) {
            direct.mkdirs();
        }

        String seriesFileName = "/["+appName+"]-"+seriesTitle+"\n"+season+"-ep"+episod + url.substring(url.lastIndexOf("."));
        String movieFileName = "/["+appName+"]-movie\n"+ movieTitle + url.substring(url.lastIndexOf("."));

        File seriesFile = new File(Environment.getExternalStorageDirectory()
                + "/"+appName+"_downloads/tv_series"+seriesFileName);
        File movieFile = new File(Environment.getExternalStorageDirectory()
                + "/"+appName+"_downloads"+movieFileName);

        DownloadManager.Request request1 = new DownloadManager.Request(Uri.parse(url));
        request1.setVisibleInDownloadsUi(false);
        request1.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            request1.allowScanningByMediaScanner();
            //request1.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
        }


        if (type.equals("tvseries")) {
            // checking the file is already download or not
            if (seriesFile.exists()) {
                new ToastMsg(this).toastIconError("File is already downloaded!");
                return;
            }
            request1.setDestinationInExternalPublicDir( "/"+appName+"_downloads/tv_series", seriesFileName);
        } else if (type.equals("movie")) {
            // checking the file is already download or not
            if (movieFile.exists()) {
                new ToastMsg(this).toastIconError("File is already downloaded!");
                return;
            }
            request1.setDestinationInExternalPublicDir( "/"+appName+"_downloads", movieFileName);
        }

        DownloadManager manager1 = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        // start the download
        manager1.enqueue(request1);
        if (DownloadManager.STATUS_SUCCESSFUL == 8) {
            new ToastMsg(this).toastIconSuccess("Download will be started soon.");
        }
    }


    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (!isFullScr) {
                isFullScr = true;
                swipeRefreshLayout.setVisibility(GONE);
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

                if (isVideo) {
                    lPlay.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));

                } else {
                    lPlay.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));

                }
            }

            // reset the orientation
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){

            if (!fullScreenByClick) {
                if (isFullScr) {

                    isFullScr = false;
                    swipeRefreshLayout.setVisibility(VISIBLE);
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

                    if (isVideo) {
                        lPlay.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, playerHeight));

                    } else {
                        lPlay.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, playerHeight));
                    }


                }

                // reset the orientation
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            }

        }

    }

    public void hideExoControlForTv() {
        exoRewind.setVisibility(GONE);
        exoForward.setVisibility(GONE);
        liveTv.setVisibility(VISIBLE);
        downloadIv.setVisibility(GONE);
        seekbarLayout.setVisibility(GONE);
    }

    public void showExoControlForTv() {
        exoRewind.setVisibility(VISIBLE);
        exoForward.setVisibility(VISIBLE);
        liveTv.setVisibility(GONE);
        seekbarLayout.setVisibility(VISIBLE);
        downloadIv.setVisibility(GONE);
        watchLiveTv.setVisibility(VISIBLE);
        liveTv.setVisibility(GONE);
        watchStatusTv.setText(getResources().getString(R.string.watching_catch_up_tv));
    }
}
