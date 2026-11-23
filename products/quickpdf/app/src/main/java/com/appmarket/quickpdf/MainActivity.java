package com.appmarket.quickpdf;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final int PICK_IMAGES = 41;
    private EditText textInput;
    private TextView status;
    private File lastPdf;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        buildScreen();
    }

    private void buildScreen() {
        int ink = 0xFF172033, muted = 0xFF64748B, accent = 0xFF3157D5;
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(22), dp(28), dp(22), dp(24));
        page.setBackgroundColor(0xFFF5F7FC);

        ScrollView scroll = new ScrollView(this);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(content);

        TextView brand = new TextView(this);
        brand.setText("QUICKPDF");
        brand.setTextColor(accent);
        brand.setTextSize(13);
        brand.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        content.addView(brand);

        TextView title = new TextView(this);
        title.setText("Your documents,\nsimplified.");
        title.setTextColor(ink);
        title.setTextSize(30);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        title.setPadding(0, dp(10), 0, dp(6));
        content.addView(title);

        TextView intro = new TextView(this);
        intro.setText("Create clean PDF files on your phone. Your documents stay on your device.");
        intro.setTextColor(muted);
        intro.setTextSize(15);
        intro.setPadding(0, 0, 0, dp(22));
        content.addView(intro);

        TextView label = new TextView(this);
        label.setText("CREATE A PDF FROM TEXT");
        label.setTextColor(ink);
        label.setTextSize(12);
        label.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        content.addView(label);

        textInput = new EditText(this);
        textInput.setHint("Type or paste your document here...");
        textInput.setTextSize(16);
        textInput.setGravity(Gravity.TOP);
        textInput.setMinLines(7);
        textInput.setPadding(dp(14), dp(12), dp(14), dp(12));
        textInput.setBackgroundColor(0xFFFFFFFF);
        LinearLayout.LayoutParams inputParams = new LinearLayout.LayoutParams(-1, dp(180));
        inputParams.setMargins(0, dp(9), 0, dp(12));
        content.addView(textInput, inputParams);

        Button makeText = button("Create PDF from text", accent);
        content.addView(makeText, buttonParams());
        makeText.setOnClickListener(v -> createTextPdf());

        TextView imageLabel = new TextView(this);
        imageLabel.setText("IMAGE TO PDF");
        imageLabel.setTextColor(ink);
        imageLabel.setTextSize(12);
        imageLabel.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        imageLabel.setPadding(0, dp(24), 0, dp(8));
        content.addView(imageLabel);

        TextView imageHelp = new TextView(this);
        imageHelp.setText("Choose one or more pictures. QuickPDF will place them into a PDF document.");
        imageHelp.setTextColor(muted);
        imageHelp.setTextSize(14);
        content.addView(imageHelp);

        Button images = button("Choose images and create PDF", 0xFF172033);
        LinearLayout.LayoutParams imageParams = buttonParams();
        imageParams.setMargins(0, dp(12), 0, dp(10));
        content.addView(images, imageParams);
        images.setOnClickListener(v -> pickImages());

        Button share = button("Share last PDF", 0xFF0F766E);
        content.addView(share, buttonParams());
        share.setOnClickListener(v -> shareLastPdf());

        status = new TextView(this);
        status.setText("Ready. No account or internet connection required.");
        status.setTextColor(muted);
        status.setTextSize(13);
        status.setPadding(0, dp(18), 0, dp(8));
        content.addView(status);

        TextView privacy = new TextView(this);
        privacy.setText("PRIVATE BY DESIGN  •  FILES ARE PROCESSED LOCALLY");
        privacy.setTextColor(muted);
        privacy.setTextSize(10);
        privacy.setPadding(0, dp(14), 0, dp(10));
        content.addView(privacy);

        page.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(page);
    }

    private Button button(String text, int color) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextColor(0xFFFFFFFF);
        b.setAllCaps(false);
        b.setTextSize(15);
        b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(color));
        return b;
    }

    private LinearLayout.LayoutParams buttonParams() {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, dp(54));
        p.setMargins(0, dp(4), 0, dp(4));
        return p;
    }

    private void createTextPdf() {
        String value = textInput.getText().toString().trim();
        if (value.isEmpty()) {
            textInput.setError("Enter some text first");
            return;
        }
        PdfDocument pdf = new PdfDocument();
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(0xFF182033);
        paint.setTextSize(12f);
        int pageNo = 1;
        PdfDocument.Page page = newPage(pdf, pageNo);
        Canvas canvas = page.getCanvas();
        int y = 58;
        int left = 48, right = 547;
        String[] paragraphs = value.split("\\n");
        for (String paragraph : paragraphs) {
            String remaining = paragraph;
            if (remaining.isEmpty()) { y += 18; continue; }
            while (!remaining.isEmpty()) {
                int count = paint.breakText(remaining, true, right - left, null);
                if (count <= 0) count = Math.min(1, remaining.length());
                canvas.drawText(remaining.substring(0, count), left, y, paint);
                remaining = remaining.substring(count).stripLeading();
                y += 19;
                if (y > 790) {
                    pdf.finishPage(page);
                    page = newPage(pdf, ++pageNo);
                    canvas = page.getCanvas();
                    y = 58;
                }
            }
            y += 8;
        }
        pdf.finishPage(page);
        savePdf(pdf, "QuickPDF-Text");
    }

    private PdfDocument.Page newPage(PdfDocument pdf, int number) {
        PdfDocument.PageInfo info = new PdfDocument.PageInfo.Builder(595, 842, number).create();
        PdfDocument.Page page = pdf.startPage(info);
        Paint heading = new Paint(Paint.ANTI_ALIAS_FLAG);
        heading.setColor(0xFF3157D5);
        heading.setTextSize(10f);
        heading.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        page.getCanvas().drawText("QUICKPDF", 48, 34, heading);
        Paint line = new Paint();
        line.setColor(0xFFE2E8F0);
        page.getCanvas().drawLine(48, 43, 547, 43, line);
        return page;
    }

    private void pickImages() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, PICK_IMAGES);
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != PICK_IMAGES || resultCode != RESULT_OK || data == null) return;
        PdfDocument pdf = new PdfDocument();
        try {
            int count = 0;
            if (data.getClipData() != null) {
                for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                    addImagePage(pdf, data.getClipData().getItemAt(i).getUri(), ++count);
                }
            } else if (data.getData() != null) {
                addImagePage(pdf, data.getData(), ++count);
            }
            if (count == 0) {
                pdf.close();
                showStatus("No readable images were selected.");
                return;
            }
            savePdf(pdf, "QuickPDF-Images");
        } catch (Exception e) {
            pdf.close();
            showStatus("Could not create PDF: " + e.getMessage());
        }
    }

    private void addImagePage(PdfDocument pdf, Uri uri, int pageNumber) throws Exception {
        Bitmap bitmap;
        try (InputStream in = getContentResolver().openInputStream(uri)) {
            bitmap = BitmapFactory.decodeStream(in);
        }
        if (bitmap == null) throw new IllegalArgumentException("Unsupported image");
        PdfDocument.Page page = pdf.startPage(new PdfDocument.PageInfo.Builder(595, 842, pageNumber).create());
        Canvas canvas = page.getCanvas();
        float maxW = 523f, maxH = 770f;
        float scale = Math.min(maxW / bitmap.getWidth(), maxH / bitmap.getHeight());
        float w = bitmap.getWidth() * scale, h = bitmap.getHeight() * scale;
        float left = (595f - w) / 2f, top = (842f - h) / 2f;
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        canvas.drawBitmap(bitmap, null, new android.graphics.RectF(left, top, left + w, top + h), p);
        pdf.finishPage(page);
        bitmap.recycle();
    }

    private void savePdf(PdfDocument pdf, String prefix) {
        try {
            File dir = getExternalFilesDir(android.os.Environment.DIRECTORY_DOCUMENTS);
            if (dir == null) dir = getFilesDir();
            if (!dir.exists() && !dir.mkdirs()) throw new IllegalStateException("Cannot create documents folder");
            String stamp = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(new Date());
            lastPdf = new File(dir, prefix + "-" + stamp + ".pdf");
            try (FileOutputStream out = new FileOutputStream(lastPdf)) { pdf.writeTo(out); }
            pdf.close();
            showStatus("PDF created: " + lastPdf.getName());
            Toast.makeText(this, "PDF created successfully", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            pdf.close();
            showStatus("Save failed: " + e.getMessage());
        }
    }

    private void shareLastPdf() {
        if (lastPdf == null || !lastPdf.exists()) {
            showStatus("Create a PDF first.");
            return;
        }
        Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".provider", lastPdf);
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("application/pdf");
        send.putExtra(Intent.EXTRA_STREAM, uri);
        send.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(send, "Share your PDF"));
    }

    private void showStatus(String message) {
        status.setText(message);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
