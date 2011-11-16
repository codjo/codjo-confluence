package net.codjo.confluence;


public class PageMock extends Page {

    public PageMock(String id, String title) {
        setData("id", id);
        setData("title", title);
    }


    public PageMock(String id, String title, String content) {
        super(id, title);
        setContent(content);
    }
}
