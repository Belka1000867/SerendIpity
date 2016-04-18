package com.example.bel.softwarefactory.ui.adapters;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.bel.softwarefactory.R;
import com.example.bel.softwarefactory.entities.AudioRecordEntity;
import com.example.bel.softwarefactory.utils.AppConstants;

import java.util.List;

public class AudioRecordsRecyclerViewAdapter extends RecyclerView.Adapter<AudioRecordsRecyclerViewAdapter.ViewHolder> {

    private MediaPlayer mediaPlayer;
    private List<AudioRecordEntity> audioRecordEntities;
    private Context context;

    public AudioRecordsRecyclerViewAdapter(Context context, List<AudioRecordEntity> audioRecordEntities) {
        this.context = context;
        this.audioRecordEntities = audioRecordEntities;
        this.mediaPlayer = new MediaPlayer();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.audio_record_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.title_textView.setText(audioRecordEntities.get(position).getFile_name());
        holder.description_textView.setText(audioRecordEntities.get(position).getDescription());
        holder.playPause_imageView.setOnClickListener(v -> {
            try {
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mediaPlayer.setDataSource(AppConstants.SERVER_ADDRESS + audioRecordEntities.get(position).getFile_path());
                mediaPlayer.prepare();
                mediaPlayer.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public int getItemCount() {
        return audioRecordEntities.size();
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView playPause_imageView;
        private TextView title_textView;
        private TextView description_textView;

        public ViewHolder(final View itemView) {
            super(itemView);
            playPause_imageView = (ImageView) itemView.findViewById(R.id.playPause_imageView);
            title_textView = (TextView) itemView.findViewById(R.id.title_textView);
            description_textView = (TextView) itemView.findViewById(R.id.description_textView);
        }
    }

    public interface IAudioRecordsRecyclerViewAdapterCallback {
        void playPause(AudioRecordEntity audioRecordEntity);
    }
}
