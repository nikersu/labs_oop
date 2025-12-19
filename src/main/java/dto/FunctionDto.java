package dto;

public class FunctionDto {
    private Long id;
    private String name;
    private String expression;
    private Long userId;

    public FunctionDto() {
    }

    public FunctionDto(Long id, String name, String expression, Long userId) {
        this.id = id;
        this.name = name;
        this.expression = expression;
        this.userId = userId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}

