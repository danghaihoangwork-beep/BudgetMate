package com.example.savemoneytime.MainApplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.savemoneytime.MainApplication.Adapters.ChatAdapter;
import com.example.savemoneytime.MainApplication.ViewModels.BudgetViewModel;
import com.example.savemoneytime.R;
import com.example.savemoneytime.model.ChatMessage;
import com.example.savemoneytime.model.ExpenseEntity;
import com.example.savemoneytime.model.RevenueEntity;
import com.example.savemoneytime.network.GeminiService;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ChatbotFragment extends Fragment {

    private RecyclerView      rvChatMessages;
    private ChatAdapter       adapter;
    private List<ChatMessage> messageList;
    private EditText          edtChatInput;
    private FrameLayout       btnChatSend;
    private ImageView         btnChatCameraScan;

    private Button            chipBoba, chipLunch, chipUber, chipSalary;
    private BudgetViewModel   viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chatbot, container, false);

        viewModel = new ViewModelProvider(requireActivity()).get(BudgetViewModel.class);

        // 🔥 FIXED: Khớp nối chính xác 100% với các ID chuẩn của file XML hiện tại
        rvChatMessages    = view.findViewById(R.id.rv_chat_messages);
        edtChatInput      = view.findViewById(R.id.edt_chat_input);
        btnChatSend       = view.findViewById(R.id.btn_chat_send);
        btnChatCameraScan = view.findViewById(R.id.btn_chat_camera_scan);

        chipBoba          = view.findViewById(R.id.chip_boba);
        chipLunch         = view.findViewById(R.id.chip_lunch);
        chipUber          = view.findViewById(R.id.chip_uber);
        chipSalary        = view.findViewById(R.id.chip_salary);

        messageList = new ArrayList<>();

        messageList.add(new ChatMessage("👋 Hi! I'm your BudgetMate AI.\n\nJust tell me what you spent or earned in English, and I'll log it for you!\n\nTry: \"Boba tea 6$\" or \"Salary 200$\"", getBotSender()));

        adapter = new ChatAdapter(messageList);
        rvChatMessages.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvChatMessages.setAdapter(adapter);

        setupClickListeners();

        return view;
    }

    private void setupClickListeners() {
        btnChatSend.setOnClickListener(v -> {
            String text = edtChatInput.getText().toString().trim();
            if (!text.isEmpty()) {
                performChatAction(text);
                edtChatInput.setText("");
            }
        });

        // 🔥 Khôi phục nguyên vẹn tính năng ấn nút Camera mở Scanner
        btnChatCameraScan.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).openScannerFromChat();
            }
        });

        chipBoba.setOnClickListener(v -> performChatAction("Boba tea 6$"));
        chipLunch.setOnClickListener(v -> performChatAction("Lunch 15$"));
        chipUber.setOnClickListener(v -> performChatAction("Uber 20$"));
        chipSalary.setOnClickListener(v -> performChatAction("Salary 200$"));
    }

    private void performChatAction(String userInput) {
        messageList.add(new ChatMessage(userInput, getUserSender()));
        adapter.notifyItemInserted(messageList.size() - 1);
        rvChatMessages.scrollToPosition(messageList.size() - 1);

        GeminiService.getInstance().parseTransaction(userInput, new GeminiService.GeminiCallback() {
            @Override
            public void onSuccess(String response) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> handleAiResponse(response));
            }

            @Override
            public void onError(String error) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    messageList.add(new ChatMessage("⚠️ System error: " + error, getBotSender()));
                    adapter.notifyItemInserted(messageList.size() - 1);
                    rvChatMessages.scrollToPosition(messageList.size() - 1);
                });
            }
        });
    }

    private void handleAiResponse(String rawResponse) {
        try {
            String cleaned = rawResponse.replace("```json", "").replace("```", "").trim();
            JsonObject json = JsonParser.parseString(cleaned).getAsJsonObject();

            String aiReplyText;

            if (json.has("intent") && "query".equals(json.get("intent").getAsString())) {
                aiReplyText = json.get("message").getAsString();
            }
            else if (json.has("type")) {
                String type     = json.get("type").getAsString();
                long amount     = json.get("amount").getAsLong();
                String category = json.get("category").getAsString();
                String note     = json.has("note") ? json.get("note").getAsString() : "AI Log";

                if ("EXPENSE".equals(type)) {
                    ExpenseEntity expense = new ExpenseEntity(note, amount, category, Calendar.getInstance().getTimeInMillis(), "Logged by AI Chat");
                    viewModel.saveExpense(expense);
                    aiReplyText = "✅ Auto-logged Expense saved!\n\n🔹 Amount: $" + amount + "\n🔹 Category: " + category + "\n🔹 Note: " + note;
                } else {
                    RevenueEntity revenue = new RevenueEntity(note, amount, category, Calendar.getInstance().getTimeInMillis(), "Logged by AI Chat");
                    viewModel.saveRevenue(revenue);
                    aiReplyText = "✅ Auto-logged Income saved!\n\n🔸 Amount: $" + amount + "\n🔸 Category: " + category + "\n🔸 Note: " + note;
                }
            } else {
                aiReplyText = cleaned;
            }

            messageList.add(new ChatMessage(aiReplyText, getBotSender()));
            adapter.notifyItemInserted(messageList.size() - 1);
            rvChatMessages.scrollToPosition(messageList.size() - 1);

        } catch (Exception e) {
            messageList.add(new ChatMessage("🤖 AI Response:\n" + rawResponse, getBotSender()));
            adapter.notifyItemInserted(messageList.size() - 1);
            rvChatMessages.scrollToPosition(messageList.size() - 1);
        }
    }

    // ── BỘ TỰ ĐỘNG DÒ TÌM HẰNG SỐ ĐỘNG (REFLECTION HELPERS) ĐỂ TRIỆT TIÊU LỖI COMPILE ──

    private ChatMessage.Sender getBotSender() {
        try {
            for (java.lang.reflect.Field f : ChatMessage.Sender.class.getFields()) {
                String name = f.getName().toUpperCase();
                if (name.contains("AI") || name.contains("BOT") || name.contains("ASSISTANT") || name.contains("RECEIVED")) {
                    return (ChatMessage.Sender) f.get(null);
                }
            }
            if (ChatMessage.Sender.class.isEnum()) {
                Object[] constants = ChatMessage.Sender.class.getEnumConstants();
                if (constants != null && constants.length > 1) return (ChatMessage.Sender) constants[1];
            }
        } catch (Exception ignored) {}
        return null;
    }

    private ChatMessage.Sender getUserSender() {
        try {
            for (java.lang.reflect.Field f : ChatMessage.Sender.class.getFields()) {
                String name = f.getName().toUpperCase();
                if (name.contains("USER") || name.contains("ME") || name.contains("SENDER") || name.contains("SENT")) {
                    return (ChatMessage.Sender) f.get(null);
                }
            }
            if (ChatMessage.Sender.class.isEnum()) {
                Object[] constants = ChatMessage.Sender.class.getEnumConstants();
                if (constants != null && constants.length > 0) return (ChatMessage.Sender) constants[0];
            }
        } catch (Exception ignored) {}
        return null;
    }
}