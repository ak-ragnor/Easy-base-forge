package com.easybase.forge.core.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

	private List<String> authors = new ArrayList<>();

	private ResponseWrapperConfig responseWrapper = new ResponseWrapperConfig();

	private String crossOrigin = null;

	private boolean slf4j = false;

	private String postGenerateCommand = null;

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

	public List<String> getAuthors() {
		return authors;
	}

	public void setAuthors(List<String> authors) {
		if (authors != null) {
			this.authors = authors;
		} else {
			this.authors = new ArrayList<>();
		}
	}

	public List<String> getAllAuthors() {
		List<String> all = new ArrayList<>(authors);

		if (author != null && !author.isBlank() && !all.contains(author)) {
			all.add(0, author);
		}

		return all;
	}

	public ResponseWrapperConfig getResponseWrapper() {
		return responseWrapper;
	}

	public void setResponseWrapper(ResponseWrapperConfig responseWrapper) {
		this.responseWrapper = Objects.requireNonNullElseGet(responseWrapper, ResponseWrapperConfig::new);
	}

	public String getCrossOrigin() {
		return crossOrigin;
	}

	public void setCrossOrigin(String crossOrigin) {
		this.crossOrigin = crossOrigin;
	}

	public boolean isSlf4j() {
		return slf4j;
	}

	public void setSlf4j(boolean slf4j) {
		this.slf4j = slf4j;
	}

	public String getPostGenerateCommand() {
		return postGenerateCommand;
	}

	public void setPostGenerateCommand(String postGenerateCommand) {
		this.postGenerateCommand = postGenerateCommand;
	}
}
