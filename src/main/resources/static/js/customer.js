/**
 * MicroLend Customer Javascript
 */

document.addEventListener('DOMContentLoaded', () => {

    
    const path = window.location.pathname;

    // Profile Page Logic
    if (path === '/customer/profile') {
        loadProfile();
        const profileForm = document.getElementById('profileForm');
        if (profileForm) {
            profileForm.addEventListener('submit', saveProfile);
        }
    }
    
    // Loans List Page
    if (path === '/customer/loans' || path === '/customer/loans/') {
        loadLoansList();
    }
    
    // New Loan Form Page
    if (path === '/customer/loans/new') {
        const applyLoanForm = document.getElementById('applyLoanForm');
        if (applyLoanForm) {
            applyLoanForm.addEventListener('submit', submitNewLoanApplication);
        }
    }
    
    // Loan Details Page (e.g. /customer/loans/1)
    if (path.match(/^\/customer\/loans\/\d+$/)) {
        const parts = path.split('/');
        const loanId = parts[parts.length - 1];
        loadLoanDetails(loanId);
        
        const deleteBtn = document.getElementById('deleteApplicationBtn');
        if (deleteBtn) deleteBtn.addEventListener('click', () => deleteLoanDraft(loanId));
        
        const submitBtn = document.getElementById('submitApplicationBtn');
        if (submitBtn) submitBtn.addEventListener('click', () => submitLoanApplication(loanId));
        
        const editBtn = document.getElementById('editApplicationBtn');
        if (editBtn) editBtn.addEventListener('click', (e) => {
            e.preventDefault();
            window.location.href = `/customer/loans/${loanId}/edit`;
        });
        
        const evalBtn = document.getElementById('evaluateEligibilityBtn');
        if (evalBtn) evalBtn.addEventListener('click', () => evaluateEligibility(loanId));
        
        const genAmortBtn = document.getElementById('generateAmortizationBtn');
        if (genAmortBtn) genAmortBtn.addEventListener('click', () => generateAmortization(loanId, genAmortBtn));
    }
    
    // Edit Loan Form Page (e.g. /customer/loans/1/edit)
    if (path.match(/^\/customer\/loans\/\d+\/edit$/)) {
        const parts = path.split('/');
        const loanId = parts[parts.length - 2];
        loadLoanForEdit(loanId);
        
        const applyLoanForm = document.getElementById('applyLoanForm');
        if (applyLoanForm) {
            applyLoanForm.addEventListener('submit', (e) => updateLoanApplication(e, loanId));
        }
        
        const cancelBtn = document.getElementById('cancelLoanBtn');
        if (cancelBtn) {
            cancelBtn.style.display = 'inline-block';
            cancelBtn.addEventListener('click', () => window.location.href = `/customer/loans/${loanId}`);
        }
    }
});

// ==========================================
// PROFILE LOGIC (Phase F3)
// ==========================================

let isProfileExists = false;

async function loadProfile() {
    const loading = document.getElementById('profileLoading');
    const missingMsg = document.getElementById('profileMissingMessage');
    const form = document.getElementById('profileForm');
    const errorBox = document.getElementById('profileError');

    try {
        const res = await apiFetch('/api/customers/me');
        loading.style.display = 'none';
        
        if (res.status === 200) {
            isProfileExists = true;
            const profile = await res.json();
            document.getElementById('fullName').value = profile.fullName;
            document.getElementById('phoneNumber').value = profile.phoneNumber;
            document.getElementById('dateOfBirth').value = profile.dateOfBirth;
            document.getElementById('address').value = profile.address;
            document.getElementById('employmentType').value = profile.employmentType;
            document.getElementById('monthlyIncome').value = profile.monthlyIncome;
            form.style.display = 'block';
        } else if (res.status === 404) {
            isProfileExists = false;
            missingMsg.style.display = 'block';
            form.style.display = 'block';
        } else {
            const err = await res.json().catch(() => ({}));
            errorBox.textContent = "Failed to load profile: " + (err.message || 'Unknown error');
            errorBox.style.display = 'block';
        }
    } catch (error) {
        loading.style.display = 'none';
        errorBox.textContent = "An error occurred while loading profile.";
        errorBox.style.display = 'block';
    }
}

async function saveProfile(e) {
    e.preventDefault();
    
    const errorBox = document.getElementById('profileError');
    const successBox = document.getElementById('profileSuccess');
    const btn = document.getElementById('saveProfileBtn');
    
    errorBox.style.display = 'none';
    successBox.style.display = 'none';
    btn.disabled = true;
    btn.textContent = 'Saving...';
    
    const payload = {
        fullName: document.getElementById('fullName').value,
        phoneNumber: document.getElementById('phoneNumber').value,
        dateOfBirth: document.getElementById('dateOfBirth').value,
        address: document.getElementById('address').value,
        employmentType: document.getElementById('employmentType').value,
        monthlyIncome: parseFloat(document.getElementById('monthlyIncome').value)
    };
    
    const url = '/api/customers' + (isProfileExists ? '/me' : '');
    const method = isProfileExists ? 'PUT' : 'POST';
    
    try {
        const res = await apiFetch(url, {
            method: method,
            body: JSON.stringify(payload)
        });
        
        if (res.status === 200 || res.status === 201) {
            successBox.style.display = 'block';
            document.getElementById('profileMissingMessage').style.display = 'none';
            isProfileExists = true;
        } else {
            const err = await res.json().catch(() => ({}));
            errorBox.textContent = "Failed to save profile: " + (err.message || 'Validation error');
            errorBox.style.display = 'block';
        }
    } catch (error) {
        errorBox.textContent = "An error occurred while saving profile.";
        errorBox.style.display = 'block';
    } finally {
        btn.disabled = false;
        btn.textContent = 'Save Profile';
    }
}


// ==========================================
// LOANS LOGIC (Phase F4)
// ==========================================

function getStatusBadgeClass(status) {
    switch(status) {
        case 'DRAFT': return 'badge-secondary';
        case 'SUBMITTED': return 'badge-info';
        case 'UNDER_REVIEW': return 'badge-review';
        case 'APPROVED': return 'badge-approved';
        case 'REJECTED': return 'badge-rejected';
        case 'CLOSED': return 'badge-closed';
        default: return 'badge-secondary';
    }
}

async function loadLoansList() {
    const loading = document.getElementById('loansLoading');
    const errorBox = document.getElementById('loansError');
    const emptyState = document.getElementById('loansEmptyState');
    const loansCard = document.getElementById('loansCard');
    const tbody = document.getElementById('loansTableBody');
    
    try {
        const res = await apiFetch('/api/loan-applications');
        loading.style.display = 'none';
        
        if (res.status === 200) {
            const loans = await res.json();
            if (loans.length === 0) {
                emptyState.style.display = 'block';
            } else {
                tbody.innerHTML = '';
                loans.forEach(loan => {
                    const row = document.createElement('tr');
                    
                    const dateDisplay = loan.createdAt ? new Date(loan.createdAt).toLocaleDateString() : 'N/A';
                    const amountDisplay = '₹' + loan.requestedAmount.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
                    
                    row.innerHTML = `
                        <td>${loan.applicationNumber || 'N/A'}</td>
                        <td>${loan.loanType}</td>
                        <td>${amountDisplay}</td>
                        <td>${loan.tenureMonths} Months</td>
                        <td><span class="badge ${getStatusBadgeClass(loan.status)}">${loan.status}</span></td>
                        <td>${dateDisplay}</td>
                        <td><a href="/customer/loans/${loan.id}" class="btn btn-secondary btn-sm">Details</a></td>
                    `;
                    tbody.appendChild(row);
                });
                loansCard.style.display = 'block';
            }
        } else {
            const err = await res.json().catch(() => ({}));
            errorBox.textContent = "Failed to load loans: " + (err.message || 'Unknown error');
            errorBox.style.display = 'block';
        }
    } catch (error) {
        loading.style.display = 'none';
        errorBox.textContent = "An error occurred while loading loans.";
        errorBox.style.display = 'block';
    }
}

async function submitNewLoanApplication(e) {
    e.preventDefault();
    const errorBox = document.getElementById('loanFormError');
    const btn = document.getElementById('saveLoanBtn');
    
    errorBox.style.display = 'none';
    btn.disabled = true;
    btn.textContent = 'Creating...';
    
    const payload = {
        loanType: document.getElementById('loanType').value,
        requestedAmount: parseFloat(document.getElementById('amount').value),
        tenureMonths: parseInt(document.getElementById('tenure').value, 10),
        purpose: document.getElementById('purpose').value
    };
    
    try {
        const res = await apiFetch('/api/loan-applications', {
            method: 'POST',
            body: JSON.stringify(payload)
        });
        
        if (res.status === 201) {
            const data = await res.json();
            window.location.href = `/customer/loans/${data.id}`;
        } else if (res.status === 409) {
            const err = await res.json().catch(() => ({}));
            // Likely CustomerProfileRequiredException
            errorBox.innerHTML = `Please complete your customer profile before applying for a loan. <a href="/customer/profile">Complete Profile</a>`;
            errorBox.style.display = 'block';
        } else {
            const err = await res.json().catch(() => ({}));
            errorBox.textContent = err.message || 'Failed to create loan application.';
            errorBox.style.display = 'block';
        }
    } catch (error) {
        errorBox.textContent = "An error occurred.";
        errorBox.style.display = 'block';
    } finally {
        btn.disabled = false;
        btn.textContent = 'Save Application';
    }
}

async function loadLoanDetails(id) {
    const loading = document.getElementById('loanDetailsLoading');
    const errorBox = document.getElementById('loanDetailsError');
    const content = document.getElementById('loanDetailsContent');
    
    try {
        const res = await apiFetch(`/api/loan-applications/${id}`);
        loading.style.display = 'none';
        
        if (res.status === 200) {
            const loan = await res.json();
            
            document.getElementById('detailLoanNumber').textContent = `Loan Details - ${loan.applicationNumber || 'N/A'}`;
            
            const badge = document.getElementById('detailStatusBadge');
            badge.textContent = loan.status;
            badge.className = `badge ${getStatusBadgeClass(loan.status)}`;
            
            document.getElementById('detailAmount').textContent = '₹' + loan.requestedAmount.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
            document.getElementById('detailTenure').textContent = loan.tenureMonths;
            document.getElementById('detailType').textContent = loan.loanType;
            document.getElementById('detailPurpose').textContent = loan.purpose;
            
            document.getElementById('detailCreated').textContent = loan.createdAt ? new Date(loan.createdAt).toLocaleString() : 'N/A';
            document.getElementById('detailSubmitted').textContent = loan.submittedAt ? new Date(loan.submittedAt).toLocaleString() : 'Not yet submitted';
            
            if (loan.status === 'DRAFT') {
                document.getElementById('draftActions').style.display = 'block';
            } else {
                document.getElementById('detailEligibilityCard').style.display = 'block';
                loadEligibility(id);
                
                if (loan.status === 'SUBMITTED') {
                    document.getElementById('submittedActions').style.display = 'block';
                }
                
                if (loan.status === 'APPROVED' || loan.status === 'CLOSED') {
                    document.getElementById('detailAmortizationCard').style.display = 'block';
                    loadAmortization(id, loan.status);
                    
                    document.getElementById('detailDisbursementCard').style.display = 'block';
                    loadDisbursement(id);
                    
                    document.getElementById('detailRepaymentCard').style.display = 'block';
                    loadRepayments(id);
                    
                    document.getElementById('detailOverdueCard').style.display = 'block';
                    loadOverdue(id);
                    
                    document.getElementById('detailSettlementCard').style.display = 'block';
                    loadSettlementQuote(id);
                }
            }
            
            content.style.display = 'block';
        } else if (res.status === 404) {
            errorBox.textContent = "Loan application not found.";
            errorBox.style.display = 'block';
        } else {
            const err = await res.json().catch(() => ({}));
            errorBox.textContent = "Failed to load details: " + (err.message || 'Unknown error');
            errorBox.style.display = 'block';
        }
    } catch (error) {
        loading.style.display = 'none';
        errorBox.textContent = "An error occurred.";
        errorBox.style.display = 'block';
    }
}

async function loadLoanForEdit(id) {
    const loading = document.getElementById('loanFormLoading');
    const errorBox = document.getElementById('loanFormError');
    const formCard = document.getElementById('loanFormCard');
    
    loading.style.display = 'block';
    formCard.style.display = 'none';
    
    try {
        const res = await apiFetch(`/api/loan-applications/${id}`);
        loading.style.display = 'none';
        
        if (res.status === 200) {
            const loan = await res.json();
            if (loan.status !== 'DRAFT') {
                errorBox.textContent = "This loan application can no longer be edited.";
                errorBox.style.display = 'block';
                return;
            }
            
            document.getElementById('loanType').value = loan.loanType;
            document.getElementById('amount').value = loan.requestedAmount;
            document.getElementById('tenure').value = loan.tenureMonths;
            document.getElementById('purpose').value = loan.purpose;
            
            formCard.style.display = 'block';
        } else {
            errorBox.textContent = "Failed to load loan for editing.";
            errorBox.style.display = 'block';
        }
    } catch(err) {
        loading.style.display = 'none';
        errorBox.textContent = "An error occurred.";
        errorBox.style.display = 'block';
    }
}

async function updateLoanApplication(e, id) {
    e.preventDefault();
    const errorBox = document.getElementById('loanFormError');
    const btn = document.getElementById('saveLoanBtn');
    
    errorBox.style.display = 'none';
    btn.disabled = true;
    btn.textContent = 'Saving...';
    
    const payload = {
        loanType: document.getElementById('loanType').value,
        requestedAmount: parseFloat(document.getElementById('amount').value),
        tenureMonths: parseInt(document.getElementById('tenure').value, 10),
        purpose: document.getElementById('purpose').value
    };
    
    try {
        const res = await apiFetch(`/api/loan-applications/${id}`, {
            method: 'PUT',
            body: JSON.stringify(payload)
        });
        
        if (res.status === 200) {
            window.location.href = `/customer/loans/${id}`;
        } else if (res.status === 409) {
            errorBox.textContent = "Application cannot be modified in its current state.";
            errorBox.style.display = 'block';
        } else {
            const err = await res.json().catch(() => ({}));
            errorBox.textContent = err.message || 'Failed to update loan application.';
            errorBox.style.display = 'block';
        }
    } catch (error) {
        errorBox.textContent = "An error occurred.";
        errorBox.style.display = 'block';
    } finally {
        btn.disabled = false;
        btn.textContent = 'Save Application';
    }
}

async function deleteLoanDraft(id) {
    if (!confirm("Are you sure you want to delete this draft?")) return;
    
    const errorBox = document.getElementById('loanDetailsError');
    const btn = document.getElementById('deleteApplicationBtn');
    
    errorBox.style.display = 'none';
    btn.disabled = true;
    
    try {
        const res = await apiFetch(`/api/loan-applications/${id}`, {
            method: 'DELETE'
        });
        
        if (res.status === 204) {
            window.location.href = '/customer/loans';
        } else if (res.status === 409) {
            const err = await res.json().catch(() => ({}));
            errorBox.textContent = err.message || "Cannot delete application in its current state.";
            errorBox.style.display = 'block';
            btn.disabled = false;
        } else {
            errorBox.textContent = "Failed to delete loan application.";
            errorBox.style.display = 'block';
            btn.disabled = false;
        }
    } catch (error) {
        errorBox.textContent = "An error occurred.";
        errorBox.style.display = 'block';
        btn.disabled = false;
    }
}

async function submitLoanApplication(id) {
    if (!confirm("Once submitted, this application cannot be edited. Continue?")) return;
    
    const errorBox = document.getElementById('loanDetailsError');
    const btn = document.getElementById('submitApplicationBtn');
    
    errorBox.style.display = 'none';
    btn.disabled = true;
    btn.textContent = 'Submitting...';
    
    try {
        const res = await apiFetch(`/api/loan-applications/${id}/submit`, {
            method: 'POST'
        });
        
        if (res.status === 200) {
            window.location.reload();
        } else if (res.status === 409) {
            const err = await res.json().catch(() => ({}));
            errorBox.textContent = err.message || "Application cannot be submitted in its current state.";
            errorBox.style.display = 'block';
            btn.disabled = false;
            btn.textContent = 'Submit Application';
        } else {
            errorBox.textContent = "Failed to submit loan application.";
            errorBox.style.display = 'block';
            btn.disabled = false;
            btn.textContent = 'Submit Application';
        }
    } catch (error) {
        errorBox.textContent = "An error occurred.";
        errorBox.style.display = 'block';
        btn.disabled = false;
        btn.textContent = 'Submit Application';
    }
}

// ==========================================
// ELIGIBILITY LOGIC (Phase F5)
// ==========================================

async function loadEligibility(applicationId) {
    const loading = document.getElementById('eligibilityLoading');
    const errorBox = document.getElementById('eligibilityError');
    const emptyMsg = document.getElementById('eligibilityEmpty');
    const content = document.getElementById('eligibilityContent');
    const badge = document.getElementById('detailDecisionBadge');
    
    loading.style.display = 'block';
    errorBox.style.display = 'none';
    emptyMsg.style.display = 'none';
    content.style.display = 'none';
    badge.style.display = 'none';
    
    try {
        const res = await apiFetch(`/api/eligibility/applications/${applicationId}`);
        loading.style.display = 'none';
        
        if (res.status === 200) {
            const data = await res.json();
            renderEligibilityData(data);
        } else if (res.status === 404) {
            emptyMsg.style.display = 'block';
        } else {
            const err = await res.json().catch(() => ({}));
            errorBox.textContent = "Failed to load eligibility: " + (err.message || 'Unknown error');
            errorBox.style.display = 'block';
        }
    } catch (error) {
        loading.style.display = 'none';
        errorBox.textContent = "An error occurred while loading eligibility.";
        errorBox.style.display = 'block';
    }
}

async function evaluateEligibility(applicationId) {
    const errorBox = document.getElementById('eligibilityError');
    const btn = document.getElementById('evaluateEligibilityBtn');
    const loading = document.getElementById('eligibilityLoading');
    const content = document.getElementById('eligibilityContent');
    const emptyMsg = document.getElementById('eligibilityEmpty');
    const badge = document.getElementById('detailDecisionBadge');
    
    errorBox.style.display = 'none';
    emptyMsg.style.display = 'none';
    content.style.display = 'none';
    badge.style.display = 'none';
    
    btn.disabled = true;
    btn.textContent = 'Evaluating...';
    loading.style.display = 'block';
    
    try {
        const res = await apiFetch(`/api/eligibility/applications/${applicationId}/evaluate`, {
            method: 'POST'
        });
        
        loading.style.display = 'none';
        
        if (res.status === 200) {
            const data = await res.json();
            renderEligibilityData(data);
        } else if (res.status === 409) {
            const err = await res.json().catch(() => ({}));
            errorBox.textContent = err.message || "Application is not in a valid state for evaluation.";
            errorBox.style.display = 'block';
        } else {
            const err = await res.json().catch(() => ({}));
            errorBox.textContent = "Evaluation failed: " + (err.message || 'Unknown error');
            errorBox.style.display = 'block';
        }
    } catch (error) {
        loading.style.display = 'none';
        errorBox.textContent = "An error occurred during evaluation.";
        errorBox.style.display = 'block';
    } finally {
        btn.disabled = false;
        btn.textContent = 'Evaluate Eligibility';
    }
}

function renderEligibilityData(data) {
    const content = document.getElementById('eligibilityContent');
    const badge = document.getElementById('detailDecisionBadge');
    const list = document.getElementById('detailReasonsList');
    
    // Format decision badge
    badge.textContent = data.decision;
    badge.className = `badge ${data.decision === 'ELIGIBLE' ? 'badge-approved' : 'badge-rejected'}`;
    badge.style.display = 'inline-block';
    
    // Fill data fields
    document.getElementById('detailEvaluatedAt').textContent = data.evaluatedAt ? new Date(data.evaluatedAt).toLocaleString() : 'N/A';
    
    const dti = data.debtToIncomeRatio != null ? (data.debtToIncomeRatio * 100).toFixed(2) + '%' : 'N/A';
    document.getElementById('detailDti').textContent = dti;
    
    const lti = data.loanToIncomeRatio != null ? (data.loanToIncomeRatio * 100).toFixed(2) + '%' : 'N/A';
    document.getElementById('detailLti').textContent = lti;
    
    const emi = data.estimatedEmi != null ? '₹' + data.estimatedEmi.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) : 'N/A';
    document.getElementById('detailEmi').textContent = emi;
    
    // Populate reasons
    list.innerHTML = '';
    if (data.reasons && data.reasons.length > 0) {
        data.reasons.forEach(reason => {
            const li = document.createElement('li');
            li.textContent = reason;
            list.appendChild(li);
        });
    } else {
        const li = document.createElement('li');
        li.textContent = 'No specific reasons provided.';
        list.appendChild(li);
    }
    
    content.style.display = 'block';
}

// ==========================================
// AMORTIZATION LOGIC (Phase F7)
// ==========================================

async function loadAmortization(applicationId, status) {
    const loading = document.getElementById('amortizationLoading');
    const errorBox = document.getElementById('amortizationError');
    const emptyMsg = document.getElementById('amortizationEmpty');
    const content = document.getElementById('amortizationContent');
    const genBtn = document.getElementById('generateAmortizationBtn');
    
    loading.style.display = 'block';
    errorBox.style.display = 'none';
    emptyMsg.style.display = 'none';
    content.style.display = 'none';
    genBtn.style.display = 'none';
    
    try {
        const res = await apiFetch(`/api/loans/${applicationId}/amortization`);
        loading.style.display = 'none';
        
        if (res.status === 200) {
            const data = await res.json();
            renderAmortizationData(data);
        } else if (res.status === 404) {
            emptyMsg.style.display = 'block';
            if (status === 'APPROVED') {
                genBtn.style.display = 'inline-block';
            }
        } else {
            const err = await res.json().catch(() => ({}));
            errorBox.textContent = "Failed to load schedule: " + (err.message || 'Unknown error');
            errorBox.style.display = 'block';
        }
    } catch (error) {
        loading.style.display = 'none';
        errorBox.textContent = "An error occurred while loading amortization.";
        errorBox.style.display = 'block';
    }
}

async function generateAmortization(applicationId, btn) {
    const errorBox = document.getElementById('amortizationError');
    const loading = document.getElementById('amortizationLoading');
    const emptyMsg = document.getElementById('amortizationEmpty');
    const content = document.getElementById('amortizationContent');
    
    errorBox.style.display = 'none';
    emptyMsg.style.display = 'none';
    content.style.display = 'none';
    
    btn.disabled = true;
    btn.textContent = 'Generating...';
    loading.style.display = 'block';
    
    try {
        const res = await apiFetch(`/api/loans/${applicationId}/amortization`, {
            method: 'POST'
        });
        
        loading.style.display = 'none';
        
        if (res.status === 201) {
            const data = await res.json();
            btn.style.display = 'none';
            renderAmortizationData(data);
        } else if (res.status === 409) {
            const err = await res.json().catch(() => ({}));
            errorBox.textContent = err.message || "Conflict: Schedule already exists or application is not APPROVED.";
            errorBox.style.display = 'block';
            
            // Try to reload just in case it was created concurrently
            loadAmortization(applicationId, 'APPROVED');
        } else {
            const err = await res.json().catch(() => ({}));
            errorBox.textContent = "Generation failed: " + (err.message || 'Unknown error');
            errorBox.style.display = 'block';
            btn.style.display = 'inline-block';
            btn.disabled = false;
            btn.textContent = 'Generate Schedule';
        }
    } catch (error) {
        loading.style.display = 'none';
        errorBox.textContent = "An error occurred during generation.";
        errorBox.style.display = 'block';
        btn.style.display = 'inline-block';
        btn.disabled = false;
        btn.textContent = 'Generate Schedule';
    }
}

function renderAmortizationData(data) {
    document.getElementById('amortPrincipal').textContent = '₹' + data.principal.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
    document.getElementById('amortRate').textContent = data.annualInterestRate.toFixed(2) + '%';
    document.getElementById('amortEmi').textContent = '₹' + data.monthlyEmi.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
    
    document.getElementById('amortCount').textContent = data.numberOfInstallments;
    document.getElementById('amortTotalInterest').textContent = '₹' + data.totalInterest.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
    document.getElementById('amortTotalPayable').textContent = '₹' + data.totalPayable.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
    
    const tbody = document.getElementById('amortizationTableBody');
    tbody.innerHTML = '';
    
    if (data.installments && data.installments.length > 0) {
        data.installments.forEach(inst => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${inst.installmentNumber}</td>
                <td>${inst.dueDate}</td>
                <td>₹${inst.openingBalance.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}</td>
                <td>₹${inst.emiAmount.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}</td>
                <td>₹${inst.principalAmount.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}</td>
                <td>₹${inst.interestAmount.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}</td>
                <td>₹${inst.closingBalance.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}</td>
            `;
            tbody.appendChild(row);
        });
    }
    
    document.getElementById('amortizationContent').style.display = 'block';
}

// ==========================================
// DISBURSEMENT LOGIC (Phase F8)
// ==========================================

async function loadDisbursement(applicationId) {
    const loading = document.getElementById('disbursementLoading');
    const errorBox = document.getElementById('disbursementError');
    const emptyMsg = document.getElementById('disbursementEmpty');
    const content = document.getElementById('disbursementContent');
    
    loading.style.display = 'block';
    errorBox.style.display = 'none';
    emptyMsg.style.display = 'none';
    content.style.display = 'none';
    
    try {
        const res = await apiFetch(`/api/loans/${applicationId}/disbursement`);
        loading.style.display = 'none';
        
        if (res.status === 200) {
            const data = await res.json();
            renderDisbursementData(data);
        } else if (res.status === 404) {
            emptyMsg.style.display = 'block';
        } else if (res.status === 403) {
            errorBox.textContent = "Unable to fetch disbursement details due to backend security restrictions.";
            errorBox.style.display = 'block';
        } else {
            const err = await res.json().catch(() => ({}));
            errorBox.textContent = "Failed to load disbursement: " + (err.message || 'Unknown error');
            errorBox.style.display = 'block';
        }
    } catch (error) {
        loading.style.display = 'none';
        errorBox.textContent = "An error occurred while loading disbursement.";
        errorBox.style.display = 'block';
    }
}

function renderDisbursementData(data) {
    document.getElementById('disbursementEmpty').style.display = 'none';
    document.getElementById('disbStatus').textContent = data.status;
    document.getElementById('disbReference').textContent = data.disbursementReference;
    document.getElementById('disbAmount').textContent = '₹' + data.disbursedAmount.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
    document.getElementById('disbAt').textContent = data.disbursedAt ? new Date(data.disbursedAt).toLocaleString() : 'N/A';
    
    document.getElementById('disbursementContent').style.display = 'block';
}

// ==========================================
// REPAYMENT LOGIC (Phase F9)
// ==========================================

async function loadRepayments(applicationId) {
    const loading = document.getElementById('repaymentLoading');
    const errorBox = document.getElementById('repaymentError');
    const content = document.getElementById('repaymentContent');
    const emptyHistory = document.getElementById('repaymentHistoryEmpty');
    const historyTable = document.getElementById('repaymentHistoryTable');
    
    loading.style.display = 'block';
    errorBox.style.display = 'none';
    content.style.display = 'none';
    
    try {
        const [summaryRes, historyRes] = await Promise.all([
            apiFetch(`/api/loans/${applicationId}/repayment-summary`),
            apiFetch(`/api/loans/${applicationId}/repayments`)
        ]);
        
        loading.style.display = 'none';
        
        if (summaryRes.status === 200 && historyRes.status === 200) {
            const summaryData = await summaryRes.json();
            const historyData = await historyRes.json();
            
            // Render Summary
            document.getElementById('repOutstanding').textContent = '₹' + summaryData.totalOutstanding.toLocaleString('en-IN', { minimumFractionDigits: 2 });
            document.getElementById('repPaid').textContent = '₹' + summaryData.totalPaid.toLocaleString('en-IN', { minimumFractionDigits: 2 });
            document.getElementById('repPayable').textContent = '₹' + summaryData.totalLoanAmount.toLocaleString('en-IN', { minimumFractionDigits: 2 });
            
            document.getElementById('repInstallmentsText').textContent = `${summaryData.paidInstallments} Paid, ${summaryData.partiallyPaidInstallments} Partially Paid, ${summaryData.remainingInstallments} Remaining`;
            
            if (summaryData.totalPenalties && summaryData.totalPenalties > 0) {
                document.getElementById('repPenalties').textContent = '₹' + summaryData.totalPenalties.toLocaleString('en-IN', { minimumFractionDigits: 2 });
                document.getElementById('repPenaltiesBox').style.display = 'block';
            } else {
                document.getElementById('repPenaltiesBox').style.display = 'none';
            }
            
            // Render History
            const tbody = document.getElementById('repaymentHistoryBody');
            tbody.innerHTML = '';
            
            if (historyData.length === 0) {
                emptyHistory.style.display = 'block';
                historyTable.style.display = 'none';
            } else {
                emptyHistory.style.display = 'none';
                historyTable.style.display = 'table';
                
                historyData.forEach(rep => {
                    const row = document.createElement('tr');
                    
                    let allocationsHtml = '';
                    if (rep.allocations && rep.allocations.length > 0) {
                        allocationsHtml = rep.allocations.map(a => 
                            `<div class="small">Inst #${a.installmentNumber}: Int ₹${a.interestAllocated.toFixed(2)}, Prin ₹${a.principalAllocated.toFixed(2)}</div>`
                        ).join('');
                    } else {
                        allocationsHtml = '<span class="text-muted">None</span>';
                    }
                    
                    row.innerHTML = `
                        <td><a href="/customer/loans/${app.id}">${escapeHTML(app.applicationNumber)}</a></td>
                        <td>${escapeHTML(app.loanType)}</td>
                        <td>₹${app.requestedAmount.toLocaleString('en-IN', { minimumFractionDigits: 2 })}</td>
                        <td>${escapeHTML(app.status)}</td>
                        <td>${new Date(app.submittedAt || app.createdAt).toLocaleDateString()}</td>
                        <td>${allocationsHtml}</td>
                    `;
                    tbody.appendChild(row);
                });
            }
            
            content.style.display = 'block';
            
        } else if (summaryRes.status === 404 || historyRes.status === 404) {
            // Disbursed but not found? Usually means not disbursed yet, but the repayment APIs return 404/409.
            errorBox.textContent = "Repayment information is not available yet.";
            errorBox.style.display = 'block';
        } else {
            const err = await summaryRes.json().catch(() => ({}));
            errorBox.textContent = "Failed to load repayments: " + (err.message || 'Unknown error');
            errorBox.style.display = 'block';
        }
    } catch (error) {
        loading.style.display = 'none';
        errorBox.textContent = "An error occurred while loading repayments.";
        errorBox.style.display = 'block';
    }
}

// ==========================================
// OVERDUE LOGIC (Phase F10)
// ==========================================

async function loadOverdue(applicationId) {
    const loading = document.getElementById('overdueLoading');
    const errorBox = document.getElementById('overdueError');
    const emptyBox = document.getElementById('overdueEmpty');
    const content = document.getElementById('overdueContent');
    const tbody = document.getElementById('overdueBody');
    
    loading.style.display = 'block';
    errorBox.style.display = 'none';
    emptyBox.style.display = 'none';
    content.style.display = 'none';
    
    try {
        const res = await apiFetch(`/api/loans/${applicationId}/overdue`);
        loading.style.display = 'none';
        
        if (res.status === 200) {
            const data = await res.json();
            if (data.length === 0) {
                emptyBox.style.display = 'block';
            } else {
                tbody.innerHTML = '';
                data.forEach(od => {
                    const row = document.createElement('tr');
                    row.innerHTML = `
                        <td>${od.installmentNumber}</td>
                        <td>${od.dueDate}</td>
                        <td class="text-danger"><strong>${od.daysOverdue}</strong></td>
                        <td>₹${od.outstandingPrincipal.toLocaleString('en-IN', { minimumFractionDigits: 2 })}</td>
                        <td class="text-danger">₹${od.penaltyAmount.toLocaleString('en-IN', { minimumFractionDigits: 2 })}</td>
                        <td><span class="badge badge-danger">${od.status}</span></td>
                    `;
                    tbody.appendChild(row);
                });
                content.style.display = 'block';
            }
        } else if (res.status === 404) {
            emptyBox.style.display = 'block'; // Or error if loan not found
        } else {
            errorBox.textContent = "Failed to load overdue information.";
            errorBox.style.display = 'block';
        }
    } catch (error) {
        loading.style.display = 'none';
        errorBox.textContent = "An error occurred while loading overdue data.";
        errorBox.style.display = 'block';
    }
}

// ==========================================
// SETTLEMENT / PREPAYMENT LOGIC (Phase F11)
// ==========================================

async function loadSettlementQuote(applicationId) {
    const loading = document.getElementById('settlementLoading');
    const errorBox = document.getElementById('settlementError');
    const emptyBox = document.getElementById('settlementEmpty');
    const content = document.getElementById('settlementContent');
    
    loading.style.display = 'block';
    errorBox.style.display = 'none';
    emptyBox.style.display = 'none';
    content.style.display = 'none';
    
    try {
        const res = await apiFetch(`/api/loans/${applicationId}/settlement-quote`);
        loading.style.display = 'none';
        
        if (res.status === 200) {
            const data = await res.json();
            document.getElementById('sqPrincipal').textContent = '₹' + data.outstandingPrincipal.toLocaleString('en-IN', { minimumFractionDigits: 2 });
            document.getElementById('sqInterest').textContent = '₹' + data.outstandingInterest.toLocaleString('en-IN', { minimumFractionDigits: 2 });
            document.getElementById('sqPenalties').textContent = '₹' + data.activePenalties.toLocaleString('en-IN', { minimumFractionDigits: 2 });
            document.getElementById('sqTotal').textContent = '₹' + data.settlementAmount.toLocaleString('en-IN', { minimumFractionDigits: 2 });
            document.getElementById('sqCalculatedAt').textContent = new Date(data.calculatedAt).toLocaleString();
            content.style.display = 'block';
        } else if (res.status === 409 || res.status === 404) {
            emptyBox.style.display = 'block';
        } else {
            errorBox.textContent = "Failed to load settlement quote.";
            errorBox.style.display = 'block';
        }
    } catch (error) {
        loading.style.display = 'none';
        errorBox.textContent = "An error occurred while loading settlement quote.";
        errorBox.style.display = 'block';
    }
}
