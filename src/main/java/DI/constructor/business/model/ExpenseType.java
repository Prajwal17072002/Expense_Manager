package DI.constructor.business.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExpenseType {

	
	@Id
	@GeneratedValue
	public Long id;
	
	@NotEmpty(message = "Please specify the type of expense")
	@Column(unique = true)
	private String expenseCategory;
	
public ExpenseType(Long id, String expenseCategory) {
		super();
		this.id = id;
		this.expenseCategory = expenseCategory;
	}



	public ExpenseType() {
		// TODO Auto-generated constructor stub
	}



	public String getExpenseCategory() {
		return expenseCategory;
	}



	public void setExpenseCategory(String expenseCategory) {
		this.expenseCategory = expenseCategory;
	}

	

}
