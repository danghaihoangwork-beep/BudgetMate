package com.example.savemoneytime.Intro.IntroViewPager;

public class OnBoardingItem {

    private final int    imageRes;
    private final String accentLabel;
    private final String title;
    private final String description;
    private final int    bgColorStart;
    private final int    bgColorEnd;

    public OnBoardingItem(int imageRes, String accentLabel, String title,
                          String description, int bgColorStart, int bgColorEnd) {
        this.imageRes     = imageRes;
        this.accentLabel  = accentLabel;
        this.title        = title;
        this.description  = description;
        this.bgColorStart = bgColorStart;
        this.bgColorEnd   = bgColorEnd;
    }

    public int    getImageRes()     { return imageRes; }
    public String getAccentLabel()  { return accentLabel; }
    public String getTitle()        { return title; }
    public String getDescription()  { return description; }
    public int    getBgColorStart() { return bgColorStart; }
    public int    getBgColorEnd()   { return bgColorEnd; }
}