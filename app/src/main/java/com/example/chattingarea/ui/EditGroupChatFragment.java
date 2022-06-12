package com.example.chattingarea.ui;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chattingarea.Constant;
import com.example.chattingarea.R;
import com.example.chattingarea.RealtimeDatabaseUtils;
import com.example.chattingarea.adapter.AddMemberToGroupChatAdapter;
import com.example.chattingarea.adapter.MemberInGroupAdapter;
import com.example.chattingarea.model.GroupDto;
import com.example.chattingarea.model.UserDto;

import java.util.ArrayList;
import java.util.List;

public class EditGroupChatFragment extends Fragment {

    public static final String GROUP_ID = "GroupId";
    public static final String GROUP_NAME = "GroupName";

    private String mGroupId;
    private String mGroupName;
    private View mRootView;

    private RecyclerView mRvMemberInGroup;
    private RecyclerView mRvAddMember;
    private ProgressBar mProgressBar;

    private MemberInGroupAdapter mMemberInGroupAdapter;
    private AddMemberToGroupChatAdapter mAddMemberToGroupChatAdapter;

    public EditGroupChatFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mGroupId = getArguments().getString(GROUP_ID);
            mGroupName = getArguments().getString(GROUP_NAME);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_edit_group_chat, container, false);
        mRvMemberInGroup = mRootView.findViewById(R.id.member_group_recycler_view);
        mRvAddMember = mRootView.findViewById(R.id.member_add_recycler_view);
        mProgressBar = mRootView.findViewById(R.id.progress);

        mAddMemberToGroupChatAdapter = new AddMemberToGroupChatAdapter();
        mMemberInGroupAdapter = new MemberInGroupAdapter();

        mRvAddMember.setAdapter(mAddMemberToGroupChatAdapter);
        mRvMemberInGroup.setAdapter(mMemberInGroupAdapter);

        mAddMemberToGroupChatAdapter.setOnClickListener(
                new AddMemberToGroupChatAdapter.ClickListener() {
                    @Override
                    public void onAddClick(UserDto userDto, int position) {

                        new AlertDialog.Builder(requireContext())
                                .setTitle("Thêm thành viên")
                                .setMessage("Bạn muốn thêm " + userDto.getName() + " vào nhóm?")
                                .setNegativeButton("Thêm", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                        RealtimeDatabaseUtils.getInstance(
                                                requireContext()).addMember(userDto.getId(),
                                                new GroupDto(mGroupId, mGroupName));
                                        Toast.makeText(requireContext(),
                                                "Thêm thành viên thành công!",
                                                Toast.LENGTH_SHORT).show();

                                        //Remove this
                                        List<UserDto> listAdd = new ArrayList<>(mAddMemberToGroupChatAdapter.getCurrentList());
                                        listAdd.remove(position);
                                        mAddMemberToGroupChatAdapter.submitList(listAdd);

                                        //Add to member in group
                                        List<UserDto> listMember = new ArrayList<>(mMemberInGroupAdapter.getCurrentList());
                                        listMember.add(userDto);
                                        mMemberInGroupAdapter.submitList(listMember);
                                    }
                                })
                                .setPositiveButton("Đóng", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                    }
                                })
                                .show();
                    }
                });

        mMemberInGroupAdapter.setOnClickListener(new MemberInGroupAdapter.ClickListener() {
            @Override
            public void onDeleteClick(UserDto userDto, int position) {

                new AlertDialog.Builder(requireContext())
                        .setTitle("Thêm thành viên")
                        .setMessage("Bạn muốn xóa " + userDto.getName() + " khỏi nhóm?")
                        .setNegativeButton("Xóa", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                RealtimeDatabaseUtils.getInstance(requireContext()).deleteMember(
                                        userDto.getId(), mGroupId);
                                Toast.makeText(requireContext(), "Xóa thành viên thành công!",
                                        Toast.LENGTH_SHORT).show();

                                //Remove this
                                List<UserDto> listMember = new ArrayList<>(mMemberInGroupAdapter.getCurrentList());
                                listMember.remove(position);
                                mMemberInGroupAdapter.submitList(listMember);

                                //Add to member in group
                                List<UserDto> listAdd = new ArrayList<>(mAddMemberToGroupChatAdapter.getCurrentList());
                                listAdd.add(userDto);
                                mAddMemberToGroupChatAdapter.submitList(listAdd);
                            }
                        })
                        .setPositiveButton("Đóng", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .show();
            }
        });

        TextView tvTitle = mRootView.findViewById(R.id.edit_group_tv_title);
        tvTitle.setText("Edit group: " + mGroupName);

        mRootView.findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requireActivity().onBackPressed();
            }
        });
        RealtimeDatabaseUtils.getInstance(requireContext()).getMemberOfGroup(mGroupId,
                new RealtimeDatabaseUtils.IGetMemberInGroup() {
                    @Override
                    public void onCompletedGetMemberInGroup(Constant.StatusRequest statusRequest,
                            List<UserDto> usersNotInGroup, List<UserDto> usersInGroup,
                            String message) {
                        mMemberInGroupAdapter.submitList(usersInGroup);
                        mAddMemberToGroupChatAdapter.submitList(usersNotInGroup);

                        mProgressBar.setVisibility(View.GONE);
                    }
                });

        return mRootView;
    }
}