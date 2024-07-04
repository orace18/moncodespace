package com.sicmagroup.tondi.ui.plaintes;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sicmagroup.tondi.Plainte;
import com.sicmagroup.tondi.R;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class PlainteAdapter extends RecyclerView.Adapter<PlainteAdapter.ViewHolder> {

    private Context context;
    private List<Plainte> plainteList;
    Boolean isPlaying = false;
    MediaPlayer mediaPlayer = new MediaPlayer();
    CountDownTimer timer;

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        public TextView date_plainte;
        public TextView duration;
        public ImageButton play;
        public ImageButton delete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            date_plainte = (TextView) itemView.findViewById(R.id.date_plainte);
            play = (ImageButton) itemView.findViewById(R.id.play_btn);
            duration = (TextView) itemView.findViewById(R.id.duree_audio);
            delete = (ImageButton) itemView.findViewById(R.id.delete_btn);
        }
    }


    public PlainteAdapter(Context context, List<Plainte> plainteList) {
        this.context = context;
        this.plainteList = plainteList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.plainte_row, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Plainte plainte = plainteList.get(position);
        Calendar cal = Calendar.getInstance(Locale.FRENCH);
        Calendar cal1 = Calendar.getInstance(Locale.FRENCH);
        cal.setTimeInMillis(plainte.getCreation());
        cal1.setTimeInMillis(plainte.getDuration());
        String date = DateFormat.format("dd/MM/yyyy à HH:mm", cal).toString();
        String date1 = DateFormat.format("mm:ss", cal1).toString();
        holder.date_plainte.setText(date);
        long o = (120 - plainte.getDuration());
        holder.duration.setText(o+" s");

        holder.play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.play.setImageResource(R.drawable.outline_stop_black_24);
                ViewDialog alertDialog = new ViewDialog();
                alertDialog.showDialog(context, date, plainte.getDuration(), holder, plainte.getFile_name());
            }
        });

        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog dialog = new Dialog(context);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setCancelable(true);
                dialog.setContentView(R.layout.dialog_attention);

                TextView titre = (TextView) dialog.findViewById(R.id.deco_title);
                titre.setText("Attention");
                TextView message_deco = (TextView) dialog.findViewById(R.id.deco_message);
                message_deco.setText("Êtes-vous sur de vouloir supprimer cette plainte?\nCette opération est irréversible.");

                Button oui = (Button) dialog.findViewById(R.id.btn_oui);
                oui.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        ContextWrapper cw = new ContextWrapper(context);
                        File directory = cw.getDir("tontine_plaintes", Context.MODE_PRIVATE);
                        File mypath=new File(directory,plainte.getFile_name());
                        mypath.delete();

                        Plainte.deleteAll(Plainte.class, "id = ? ", ""+plainte.getId());
                        plainteList.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, plainteList.size());
                        Toast.makeText(context, "Plainte supprimée", Toast.LENGTH_SHORT).show();

                        dialog.dismiss();
                    }
                });

                Button non = (Button) dialog.findViewById(R.id.btn_non);
                non.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.cancel();
                    }
                });

                dialog.show();

            }
        });
    }

    public class ViewDialog {
        void showDialog(Context c, String title_playing, long duration, ViewHolder holder, String path){
            Dialog dialog = new Dialog(c);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(true);
            dialog.setContentView(R.layout.playing_plainte_dialog);
            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    dialog.dismiss();
                    holder.play.setImageResource(R.drawable.outline_play_arrow_black_24);
                    mediaPlayer.release();
                    mediaPlayer =  new MediaPlayer();
                }
            });

            ProgressBar progressBar = dialog.findViewById(R.id.progresseBar_playing);
            TextView title = dialog.findViewById(R.id.title_dialog_playing);
            title.setText(title_playing);
            progressBar.setMax((int)(120000 - duration * 1000) / 1000);

            ContextWrapper cw = new ContextWrapper(context);
            File directory = cw.getDir("tontine_plaintes", Context.MODE_PRIVATE);
            File mypath=new File(directory,path);

            if(mediaPlayer.isPlaying())
            {
                mediaPlayer.release();
                mediaPlayer =  new MediaPlayer();
            }
            try {

                mediaPlayer.setDataSource(mypath.getPath());
                mediaPlayer.prepare();
                mediaPlayer.start();

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(context, "Fichier introuvable !", Toast.LENGTH_SHORT).show();

            }


            timer = new CountDownTimer((120000 - duration * 1000), 1000) {

                public void onTick(long millisUntilFinished) {
                    progressBar.setProgress((int)(120000 / 1000 - duration ) - (int)millisUntilFinished / 1000);
                }

                public void onFinish() {
                    dialog.dismiss();
                    holder.play.setImageResource(R.drawable.outline_play_arrow_black_24);
                    mediaPlayer.release();
                    mediaPlayer =  new MediaPlayer();
                }

            }.start();
            dialog.show();



        }
    }

    @Override
    public int getItemCount() {
        return plainteList.size();
    }


}
