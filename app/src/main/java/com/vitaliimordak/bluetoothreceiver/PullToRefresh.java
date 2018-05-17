package com.vitaliimordak.bluetoothreceiver;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

/**
 * Created by Vitalii on 17.12.2017.
 */

public class PullToRefresh extends LinearLayout{
    private OnRefreshListener refresh;
    private ListView list;
    private ProgressBar bar_indeterminate;
    private ProgressBar half_bar1;
    private ProgressBar half_bar2;
    private LinearLayout layout_half_bars;
    private LinearLayout layout_bar_indeterminate;
    private MyListener listener = new MyListener();
    private Context context;

    public PullToRefresh(Context context) {
        super(context);
        init(context);
    }

    public PullToRefresh(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void setListView(ListView list){
        this.list = list;
        this.list.setOnTouchListener(listener);
    }

    public void setColor(int color){
        half_bar1.getProgressDrawable().setColorFilter(color, Mode.SRC_IN);
        half_bar2.getProgressDrawable().setColorFilter(color, Mode.SRC_IN);
        bar_indeterminate.getIndeterminateDrawable().setColorFilter(color, Mode.SRC_IN);
    }

    public void setSlide(float n){
        listener.SLIDE=n;
    }

    public void refreshComplete(){
        ((Activity)context).runOnUiThread(new Runnable(){
            public void run(){
                listener.STATE_REFRESHING=false;
                layout_half_bars.setVisibility(View.VISIBLE);
                layout_bar_indeterminate.setVisibility(View.INVISIBLE);

                half_bar1.setIndeterminate(false);
                half_bar1.setMax((int) listener.SLIDE);
                half_bar1.setProgress(0);

                half_bar2.setIndeterminate(false);
                half_bar2.setMax((int) listener.SLIDE);
                half_bar2.setProgress(0);
            }
        });

    }

    public void setOnRefreshListener(OnRefreshListener listener){
        this.refresh = listener;
    }

    public interface OnRefreshListener{
        void onRefresh();
    }

    private void init(Context context){
        this.context = context;

        super.setOrientation(LinearLayout.VERTICAL);

        bar_indeterminate = new ProgressBar(this.getContext(), null, android.R.attr.progressBarStyleHorizontal);
        bar_indeterminate.setIndeterminate(true);
        bar_indeterminate.setBackgroundColor(Color.TRANSPARENT);
        LinearLayout.LayoutParams p = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        p.setMargins(0, -10, 0, -10);

        layout_bar_indeterminate = new LinearLayout(this.getContext());
        layout_bar_indeterminate.setOrientation(LinearLayout.HORIZONTAL);
        layout_bar_indeterminate.addView(bar_indeterminate, p);
        layout_bar_indeterminate.setVisibility(View.INVISIBLE);

        half_bar1 = new ProgressBar(this.getContext(), null, android.R.attr.progressBarStyleHorizontal);
        half_bar2 = new ProgressBar(this.getContext(), null, android.R.attr.progressBarStyleHorizontal);
        half_bar1.setMax((int) listener.SLIDE);
        half_bar2.setMax((int) listener.SLIDE);
        half_bar1.setRotationY(180);

        LayoutParams p2 = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        p2.setMargins(0, -10, 0, -10);
        p2.weight=1;

        layout_half_bars = new LinearLayout(this.getContext());
        layout_half_bars.setOrientation(LinearLayout.HORIZONTAL);
        layout_half_bars.addView(half_bar1, p2);
        layout_half_bars.addView(half_bar2, p2);

        RelativeLayout layout_pull_to_refresh = new RelativeLayout(this.getContext());
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        layout_pull_to_refresh.addView(layout_bar_indeterminate, params);
        layout_pull_to_refresh.addView(layout_half_bars, params);

        super.addView(layout_pull_to_refresh, 0);
    }

    private class MyListener implements OnTouchListener{
        private float startY=-1;
        public float SLIDE=500;
        public boolean STATE_REFRESH_ENABLED=false;
        public boolean STATE_REFRESHING=false;
        public boolean STATE_MOVE=false;

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            float y = event.getY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_MOVE: {
                    STATE_MOVE = ( y - startY > SLIDE);
                    if(!STATE_MOVE && STATE_REFRESH_ENABLED && !STATE_REFRESHING){
                        half_bar1.setProgress((int) (y-startY));
                        half_bar2.setProgress((int) (y-startY));
                    }
                    else{
                        if(STATE_MOVE && STATE_REFRESH_ENABLED && !STATE_REFRESHING){
                            layout_half_bars.setVisibility(View.INVISIBLE);
                            layout_bar_indeterminate.setVisibility(View.VISIBLE);
                        }
                    }
                }
                break;

                case MotionEvent.ACTION_DOWN: {
                    startY = y;
                    STATE_REFRESH_ENABLED = (list.getFirstVisiblePosition() == 0);
                }
                break;

                case MotionEvent.ACTION_UP: {
                    if (STATE_MOVE && STATE_REFRESH_ENABLED && !STATE_REFRESHING) {
                        STATE_REFRESHING=true;
                        new Thread(){
                            public void run(){
                                refresh.onRefresh();
                            }
                        }.start();
                    }
                    else if(!STATE_MOVE && STATE_REFRESH_ENABLED && !STATE_REFRESHING){
                        int n=half_bar1.getProgress();
                        for (int i=0 ; i<=n ; i++){
                            half_bar1.setProgress(n-i);
                            half_bar2.setProgress(n-i);
                        }
                    }
                }
                break;
            }
            return false;
        }
    }
}
