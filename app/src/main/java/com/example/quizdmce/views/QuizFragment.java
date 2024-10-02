package com.example.quizdmce.views;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import com.example.quizdmce.Model.QuestionModel;
import com.example.quizdmce.R;
import com.example.quizdmce.viewmodel.QuestionViewModel;

import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

public class QuizFragment extends Fragment implements View.OnClickListener {

    private QuestionViewModel viewModel;
    private NavController navController;
    private ProgressBar progressBar;
    private Button option1Btn, option2Btn, option3Btn, nextQueBtn;
    private TextView questionTv, ansFeedBackTv, questionNumberTv, timerCountTv;
    private ImageView closeQuizBtn;
    private String quizId;
    private long totalQuestions;
    private int currentQueNo = 0;
    private boolean canAnswer = false;
    private long timer;
    private CountDownTimer countDownTimer;
    private int notAnswered = 0;
    private int correctAnswer = 0;
    private int wrongAnswer = 0;
    private String answer = "";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory
                .getInstance(getActivity().getApplication())).get(QuestionViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_quizragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);
        closeQuizBtn = view.findViewById(R.id.imageView3);
        option1Btn = view.findViewById(R.id.option1Btn);
        option2Btn = view.findViewById(R.id.option2Btn);
        option3Btn = view.findViewById(R.id.option3Btn);
        nextQueBtn = view.findViewById(R.id.nextQueBtn);
        ansFeedBackTv = view.findViewById(R.id.ansFeedbackTv);
        questionTv = view.findViewById(R.id.quizQuestionTv);
        timerCountTv = view.findViewById(R.id.countTimeQuiz);
        questionNumberTv = view.findViewById(R.id.quizQuestionsCount);
        progressBar = view.findViewById(R.id.quizCoutProgressBar);

        quizId = QuizFragmentArgs.fromBundle(getArguments()).getQuizId();
        viewModel.setQuizId(quizId);
        viewModel.getRandomQuestions(); // Fetch random questions

        option1Btn.setOnClickListener(this);
        option2Btn.setOnClickListener(this);
        option3Btn.setOnClickListener(this);
        nextQueBtn.setOnClickListener(this);

        closeQuizBtn.setOnClickListener(v -> navController.navigate(R.id.action_quizragment_to_listFragment));

        loadData();
    }

    private void loadData() {
        enableOptions();
        viewModel.getRandomQuestionsLiveData().observe(getViewLifecycleOwner(), questionModels -> {
            totalQuestions = questionModels.size(); // Update total questions
            loadQuestions(1, questionModels); // Load the first question
        });
    }

    private void enableOptions() {
        option1Btn.setVisibility(View.VISIBLE);
        option2Btn.setVisibility(View.VISIBLE);
        option3Btn.setVisibility(View.VISIBLE);

        // Enable buttons, hide feedback TV, hide nextQuiz btn
        option1Btn.setEnabled(true);
        option2Btn.setEnabled(true);
        option3Btn.setEnabled(true);
        resetOptions();

        ansFeedBackTv.setVisibility(View.INVISIBLE);
        nextQueBtn.setVisibility(View.INVISIBLE);
    }

    private void loadQuestions(int i, List<QuestionModel> questionModels) {
        currentQueNo = i;
        questionTv.setText(String.valueOf(currentQueNo) + ") " + questionModels.get(i - 1).getQuestion());
        option1Btn.setText(questionModels.get(i - 1).getOption_a());
        option2Btn.setText(questionModels.get(i - 1).getOption_b());
        option3Btn.setText(questionModels.get(i - 1).getOption_c());
        timer = questionModels.get(i - 1).getTimer();
        answer = questionModels.get(i - 1).getAnswer();

        questionNumberTv.setText(String.valueOf(currentQueNo));
        startTimer();
        canAnswer = true;
    }

    private void startTimer() {
        timerCountTv.setText(String.valueOf(timer));
        progressBar.setVisibility(View.VISIBLE);

        countDownTimer = new CountDownTimer(timer * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // Update time
                timerCountTv.setText(millisUntilFinished / 1000 + "");
                Long percent = millisUntilFinished / (timer * 10);
                progressBar.setProgress(percent.intValue());
            }

            @Override
            public void onFinish() {
                canAnswer = false;
                ansFeedBackTv.setText("Times Up !! No answer selected");
                notAnswered++;
                showNextBtn();
            }
        }.start();
    }

    private void showNextBtn() {
        if (currentQueNo == totalQuestions) {
            nextQueBtn.setText("Submit");
            nextQueBtn.setEnabled(true);
            nextQueBtn.setVisibility(View.VISIBLE);
        } else {
            nextQueBtn.setVisibility(View.VISIBLE);
            nextQueBtn.setEnabled(true);
            ansFeedBackTv.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();

        if (viewId == R.id.option1Btn) {
            verifyAnswer(option1Btn);
        } else if (viewId == R.id.option2Btn) {
            verifyAnswer(option2Btn);
        } else if (viewId == R.id.option3Btn) {
            verifyAnswer(option3Btn);
        } else if (viewId == R.id.nextQueBtn) {
            if (currentQueNo == totalQuestions) {
                submitResults();
            } else {
                currentQueNo++;
                loadQuestions(currentQueNo, viewModel.getRandomQuestionsLiveData().getValue()); // Pass the list
                resetOptions();
            }
        }
    }

    private void resetOptions() {
        ansFeedBackTv.setVisibility(View.INVISIBLE);
        nextQueBtn.setVisibility(View.INVISIBLE);
        nextQueBtn.setEnabled(false);
        option1Btn.setBackground(ContextCompat.getDrawable(getContext(), R.color.light_sky));
        option2Btn.setBackground(ContextCompat.getDrawable(getContext(), R.color.light_sky));
        option3Btn.setBackground(ContextCompat.getDrawable(getContext(), R.color.light_sky));
    }

    private void submitResults() {
        HashMap<String, Object> resultMap = new HashMap<>();
        resultMap.put("correct", correctAnswer);
        resultMap.put("wrong", wrongAnswer);
        resultMap.put("notAnswered", notAnswered);

        viewModel.addResults(resultMap);

        QuizFragmentDirections.ActionQuizragmentToResultFragment action =
                QuizFragmentDirections.actionQuizragmentToResultFragment();
        action.setQuizId(quizId);
        navController.navigate(action);
    }

    private void verifyAnswer(Button button) {
        if (canAnswer) {
            if (answer.equals(button.getText())) {
                button.setBackground(ContextCompat.getDrawable(getContext(), R.color.green));
                correctAnswer++;
                ansFeedBackTv.setText("Correct Answer");
            } else {
                button.setBackground(ContextCompat.getDrawable(getContext(), R.color.red));
                wrongAnswer++;
                ansFeedBackTv.setText("Wrong Answer \nCorrect Answer :" + answer);
            }
            ansFeedBackTv.setVisibility(View.VISIBLE);
        }
        canAnswer = false;
        countDownTimer.cancel();
        showNextBtn();
    }
}
