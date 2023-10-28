package DI.constructor.web.controller;

import DI.constructor.business.model.Expense;
import DI.constructor.business.model.ExpenseType;
import DI.constructor.business.services.ExpenseService;
import DI.constructor.business.services.ExpenseTypeService;
import DI.constructor.web.helpers.Helpers;
import DI.constructor.business.exceptions.ExpenseTypeAlreadyExistsException;

import jakarta.validation.Valid;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Month;

@Controller
@RequestMapping
public class ExpenseController {

    private final ExpenseService expenseService;
    private final ExpenseTypeService expenseTypeService;
    private static final int PAGE_SIZE = 8; //number of records per page

    public ExpenseController(ExpenseService expenseService, ExpenseTypeService expenseTypeService) {
        this.expenseService = expenseService;
        this.expenseTypeService = expenseTypeService;
    }


   
    @ModelAttribute("totalAmount")
    public BigDecimal getTotalAmount(){
        Iterable<Expense> expenses = expenseService.findAll();
        return expenseService.getTotalAmount(expenses);
    }


   
    @ModelAttribute("expenses")
    public Page<Expense> getExpenses(@PageableDefault(size = PAGE_SIZE) Pageable page){
        return expenseService.findAll(page);
    }

    @GetMapping("/expenses")
    public String showExpenses(){
        return "/expenses";
    }

    @ModelAttribute("expenseTypes")
    public Iterable<ExpenseType> getExpenseTypes() {
        return expenseTypeService.findAll();
    }

    @ModelAttribute
    public Expense getExpense(){
        return new Expense();
    }

    @ModelAttribute
    public ExpenseType getExpenseType(){
        return new ExpenseType();
    }

    @GetMapping("/newExpenseType")
    public String showExpenseTypes(){
        return "/newExpenseType";
    }


   
    @PostMapping("/newExpenseType")
    public String addExpenseType(@Valid ExpenseType expenseType, Errors errors, Model model){
        if (errors.hasErrors()) {
            return "newExpenseType";
        }
        try {
            expenseTypeService.save(expenseType);
        } catch (ExpenseTypeAlreadyExistsException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "newExpenseType";
        }
        return "redirect:/newExpenseType";
    }


    
    @PostMapping(value = "expenses/delete/individual/{id}")
    public String deleteExpense(@PathVariable("id") Long id){
        expenseService.deleteById(id);
        return "redirect:/expenses";
    }

    
    @PostMapping(value = "newExpenseType/delete/{id}")
    public String deleteExpenseType(@PathVariable("id") Long id){
        expenseTypeService.deleteById(id);
        return "redirect:/newExpenseType";
    }

  
    @PostMapping("/AddExpense")
    public String addExpense(@Valid Expense expense, Errors errors){
        if (errors.hasErrors()) {
            return "expenses"; 
        }
        expenseService.save(expense);

        return "redirect:/expenses";
    }


    
    @GetMapping("/update/{id}")
    public String showUpdateExpenseForm(@PathVariable String id, Model model) {
        Long longId = Long.parseLong(id);

        Expense expense = expenseService.findById(longId);
        model.addAttribute("expense", expense);

        return "updateExpense";

    }

    @PostMapping("/update")
    public String updateExpense(@Valid Expense expense, Errors errors) {
        if (errors.hasErrors()) {
            return "updateExpense";
        }
        expenseService.save(expense); // Save the updated expense object to the database
        return "redirect:/expenses";
    }

   
    @GetMapping("/expenses/filter")
    public String showFilteredExpenses(@RequestParam(name = "year", required = false) Integer year,
                                       @RequestParam(name = "month", required = false) Month month,
                                       @RequestParam(name = "expenseTypeFilter", required = false) String expenseType,
                                       Model model, @PageableDefault(size = PAGE_SIZE) Pageable page) {

        Page<Expense> expenses;
        String monthToDisplay = null;
        String yearToDisplay = null;

        // If all filters are provided (year, month, and expense type)
        if (year != null && month != null && expenseType != null && !expenseType.isEmpty()) {
            expenses = expenseService.getExpensesByYearMonthAndType(year, month, expenseType, page);
            monthToDisplay = Helpers.toSentenceCase(month.toString());
            yearToDisplay = year.toString();
        }
        else if (year != null && month != null) {
            expenses = expenseService.getExpensesByYearMonth(year, month, page);
            monthToDisplay = Helpers.toSentenceCase(month.toString());
            yearToDisplay = year.toString();
        }
        else if (expenseType != null && !expenseType.isEmpty()) {
            expenses = expenseService.getExpensesByType(expenseType, page);
        }

        else {
            expenses = expenseService.findAll(page);
        }

        model.addAttribute("expenses", expenses);
        model.addAttribute("month", monthToDisplay);
        model.addAttribute("year", yearToDisplay);
        model.addAttribute("expenseType", expenseType);

        return "expenses";
    }

    
    @GetMapping("/downloadExpenses")
    public ResponseEntity<Resource> downloadExpenses() {

    	Iterable<Expense> expenses = expenseService.findAll();

        String csvData = expenseService.convertToCSV(expenses);

        ByteArrayResource resource = new ByteArrayResource(csvData.getBytes());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=expenses.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .contentLength(resource.contentLength())
                .body(resource);
    }



}

