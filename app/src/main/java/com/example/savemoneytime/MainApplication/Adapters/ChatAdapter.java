package com.example.savemoneytime.MainApplication.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.savemoneytime.R;
import com.example.savemoneytime.model.ChatMessage;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_USER = 0;
    private static final int TYPE_AI   = 1;

    private final List<ChatMessage> messages;

    public ChatAdapter(List<ChatMessage> messages) {
        this.messages = messages;
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).isUser() ? TYPE_USER : TYPE_AI;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_USER) {
            View v = inflater.inflate(R.layout.item_chat_user, parent, false);
            return new UserViewHolder(v);
        } else {
            View v = inflater.inflate(R.layout.item_chat_ai, parent, false);
            return new AiViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage msg  = messages.get(position);

        if (holder instanceof UserViewHolder) {
            UserViewHolder uh = (UserViewHolder) holder;
            uh.tvMessage.setText(msg.getContent());
            // Đã lược bỏ tvTime để khớp với thiết kế Premium mới
        } else if (holder instanceof AiViewHolder) {
            AiViewHolder ah = (AiViewHolder) holder;
            ah.tvMessage.setText(msg.getContent());
            // Đã lược bỏ tvTime để khớp với thiết kế Premium mới
        }
    }

    @Override
    public int getItemCount() { return messages.size(); }

    public void addMessage(ChatMessage message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    // 🔥 CẬP NHẬT ID MỚI & LƯỢC BỎ BIẾN TIME CHO BONG BÓNG USER
    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage;
        UserViewHolder(@NonNull View v) {
            super(v);
            tvMessage = v.findViewById(R.id.tv_chat_text_user);
        }
    }

    // 🔥 CẬP NHẬT ID MỚI & LƯỢC BỎ BIẾN TIME CHO BONG BÓNG AI
    static class AiViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage;
        AiViewHolder(@NonNull View v) {
            super(v);
            tvMessage = v.findViewById(R.id.tv_chat_text_ai);
        }
    }
}