package com.example.quizdmce.viewmodel;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.quizdmce.Model.QuestionModel;
import com.example.quizdmce.repository.QuestionRepository;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class QuestionViewModel extends ViewModel implements
        QuestionRepository.OnQuestionLoad,
        QuestionRepository.OnResultAdded,
        QuestionRepository.OnResultLoad,
        QuestionRepository.OnQuestionsFetch {

    private MutableLiveData<List<QuestionModel>> questionMutableLiveData;
    private MutableLiveData<List<QuestionModel>> randomQuestionsLiveData;
    private MutableLiveData<HashMap<String, Long>> resultMutableLiveData;
    private QuestionRepository repository;

    public QuestionViewModel() {
        questionMutableLiveData = new MutableLiveData<>();
        randomQuestionsLiveData = new MutableLiveData<>();
        resultMutableLiveData = new MutableLiveData<>();
        repository = new QuestionRepository(this, this, this, this); // Passing new interface
    }

    public MutableLiveData<HashMap<String, Long>> getResultMutableLiveData() {
        return resultMutableLiveData;
    }

    public MutableLiveData<List<QuestionModel>> getQuestionMutableLiveData() {
        return questionMutableLiveData;
    }

    public MutableLiveData<List<QuestionModel>> getRandomQuestionsLiveData() {
        return randomQuestionsLiveData;
    }

    public void setQuizId(String quizId) {
        repository.setQuizId(quizId);
    }

    public void getResults() {
        repository.getResults();
    }

    public void addResults(HashMap<String, Object> resultMap) {
        repository.addResults(resultMap);
    }

    // Original method to get all questions
    public void getQuestions() {
        repository.getQuestions();
    }

    // New method to get random questions
    public void getRandomQuestions() {
        repository.getAllQuestions(); // Triggers fetching all questions
    }

    // Callback from QuestionRepository when all questions are loaded
    @Override
    public void onAllQuestionsFetched(List<QuestionModel> allQuestions) {
        List<QuestionModel> randomQuestions = getRandomSubset(allQuestions, 10);
        randomQuestionsLiveData.setValue(randomQuestions);
    }

    // Helper method to get a random subset of questions
    private List<QuestionModel> getRandomSubset(List<QuestionModel> list, int count) {
        Collections.shuffle(list); // Shuffle the list randomly
        return list.subList(0, Math.min(count, list.size())); // Return the first 'count' elements
    }

    // Callbacks for OnQuestionLoad interface
    @Override
    public void onLoad(List<QuestionModel> questionModels) {
        questionMutableLiveData.setValue(questionModels);
    }

    @Override
    public boolean onSubmit() {
        return true;
    }

    // Callbacks for OnResultLoad interface
    @Override
    public void onResultLoad(HashMap<String, Long> resultMap) {
        resultMutableLiveData.setValue(resultMap);
    }

    // Error callback for any interface
    @Override
    public void onError(Exception e) {
        Log.d("QuizError", "onError: " + e.getMessage());
    }
}
