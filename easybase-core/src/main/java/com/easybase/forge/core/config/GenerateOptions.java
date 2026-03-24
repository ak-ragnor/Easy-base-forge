package com.easybase.forge.core.config;

/**
 * Maps the {@code generate} block in {@code easybase-config.yaml}.
 */
public class GenerateOptions {

	private boolean delegateImpl = false;
	private ResponseEntityMode responseEntityWrapping = ResponseEntityMode.ALWAYS;
	private boolean beanValidation = true;
	private PaginationMode pagination = PaginationMode.NONE;
	private boolean addGeneratedAnnotation = true;
	private String author = "";

	public boolean isDelegateImpl() {
		return delegateImpl;
	}

	public void setDelegateImpl(boolean delegateImpl) {
		this.delegateImpl = delegateImpl;
	}

	public ResponseEntityMode getResponseEntityWrapping() {
		return responseEntityWrapping;
	}

	public void setResponseEntityWrapping(ResponseEntityMode responseEntityWrapping) {
		this.responseEntityWrapping = responseEntityWrapping;
	}

	public boolean isBeanValidation() {
		return beanValidation;
	}

	public void setBeanValidation(boolean beanValidation) {
		this.beanValidation = beanValidation;
	}

	public PaginationMode getPagination() {
		return pagination;
	}

	public void setPagination(PaginationMode pagination) {
		this.pagination = pagination;
	}

	public boolean isAddGeneratedAnnotation() {
		return addGeneratedAnnotation;
	}

	public void setAddGeneratedAnnotation(boolean addGeneratedAnnotation) {
		this.addGeneratedAnnotation = addGeneratedAnnotation;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}
}
