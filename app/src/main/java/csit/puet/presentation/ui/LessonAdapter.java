package csit.puet.presentation.ui;

import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.ScrollingMovementMethod;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import csit.puet.R;
import csit.puet.data.model.Lesson;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LessonAdapter extends RecyclerView.Adapter<LessonAdapter.ViewHolder> {

    private final List<Lesson> lessons;

    public LessonAdapter(List<Lesson> lessons) {
        this.lessons = lessons;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lesson, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Lesson lesson = lessons.get(position);

        SpannableString spannableString = new SpannableString(lesson.getLesson() + " (" + lesson.getLessonType() + ")");
        spannableString.setSpan(new StyleSpan(Typeface.BOLD), 0, lesson.getLesson().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        holder.tvScheduleDate.setText(formatToCustomFormat(lesson.getDate()));
        holder.tvPairNumber.setText(getPairNumberAndTime(lesson.getNum()));
        holder.tvClassName.setText(spannableString);
        holder.tvGroupName.setText(lesson.getGroup());
        holder.tvClassroom.setText(lesson.getRoom().equals("дом_ПК") ? "дистанційно" : lesson.getRoom());
        holder.tvTeacherName.setText(lesson.getTeacher());

        if (position == 0 || !lessons.get(position - 1).getDate().equals(lesson.getDate())) {
            holder.tvScheduleDate.setVisibility(View.VISIBLE);
        } else {
            holder.tvScheduleDate.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return lessons.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvScheduleDate, tvPairNumber, tvClassName, tvGroupName, tvClassroom, tvTeacherName;

        public ViewHolder(View itemView) {
            super(itemView);
            tvScheduleDate = itemView.findViewById(R.id.tvScheduleDate);
            tvPairNumber = itemView.findViewById(R.id.tvPairNumber);
            tvClassName = itemView.findViewById(R.id.tvClassName);
            tvGroupName = itemView.findViewById(R.id.tvGroupName);
            tvGroupName.setMovementMethod(new ScrollingMovementMethod());
            tvClassroom = itemView.findViewById(R.id.tvClassroom);
            tvTeacherName = itemView.findViewById(R.id.tvTeacherName);
        }
    }

    public static SpannableString getPairNumberAndTime(int pairNumber) {
        String pairText;
        String time;
        switch (pairNumber) {
            case 1:
                pairText = "1 пара ";
                time = "(8:00-9:20)";
                break;
            case 2:
                pairText = "2 пара ";
                time = "(9:30-10:50)";
                break;
            case 3:
                pairText = "3 пара ";
                time = "(11:00-12:20)";
                break;
            case 4:
                pairText = "4 пара ";
                time = "(12:40-14:00)";
                break;
            case 5:
                pairText = "5 пара ";
                time = "(14:10-15:30)";
                break;
            case 6:
                pairText = "6 пара ";
                time = "(15:40-17:00)";
                break;
            case 7:
                pairText = "7 пара ";
                time = "(17:05-18:25)";
                break;
            case 8:
                pairText = "8 пара ";
                time = "(18:30-19:50)";
                break;
            case 9:
                pairText = "9 пара ";
                time = "(19:55-21:15)";
                break;
            case 10:
                pairText = "10 пара ";
                time = "(21:20-22:40)";
                break;
            default:
                pairText = "Помилковий номер пари ";
                time = "";
        }
        SpannableString spannableString = new SpannableString(pairText + time);
        spannableString.setSpan(new StyleSpan(Typeface.BOLD), 0, pairText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        return spannableString;
    }

    public static String formatToCustomFormat(String dateString) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            Date date = inputFormat.parse(dateString);
            Calendar calendar = Calendar.getInstance();
            assert date != null;
            calendar.setTime(date);
            SimpleDateFormat outputFormat = new SimpleDateFormat("EEEE - dd.MM.yyyy", new Locale("uk", "UA"));
            return outputFormat.format(calendar.getTime());
        } catch (Exception e) {
            e.printStackTrace();
            return dateString;
        }
    }
}
