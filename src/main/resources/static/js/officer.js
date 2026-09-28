/**
 * MicroLend Officer Javascript
 */

document.addEventListener('DOMContentLoaded', () => {

    
    const path = window.location.pathname;
    
    // Officer Queue and Dashboard
    if (path === '/officer/applications' || path === '/officer/applications/' || path === '/officer/dashboard' || path === '/officer/dashboard/') {
        loadOfficerQueue(path.includes('dashboard'));
    }
    
    // Application Details
    if (path.match(/^\/officer\/applications\/\d+$/)) {
        const parts = path.split('/');
        const loanId = parts[parts.length - 1];
        loadOfficerApplication(loanId);
        
        const startReviewBtn = document.getElementById('startReviewBtn');
        if (startReviewBtn) startReviewBtn.addEventListener('click', () => startReview(loanId));
        
        const approveBtn = document.getElementById('approveBtn');
        if (approveBtn) approveBtn.addEventListener('click', () => approveApplication(loanId));
        
        const rejectBtn = document.getElementById('rejectBtn');
        const rejectFormContainer = document.getElementById('rejectFormContainer');
        const cancelRejectBtn = document.getElementById('cancelRejectBtn');
        const confirmRejectBtn = document.getElementById('confirmRejectBtn');
        
        if (rejectBtn) {
            rejectBtn.addEventListener('click', () => {
                rejectFormContainer.style.display = 'block';
                document.getElementById('actionCard').style.display = 'none';
            });
        }
        
        if (cancelRejectBtn) {
            cancelRejectBtn.addEventListener('click', () => {
                rejectFormContainer.style.display = 'none';
                document.getElementById('actionCard').style.display = 'block';
                document.getElementById('rejectionReasonInput').value = '';
            });
        }
        
        if (confirmRejectBtn) {
            confirmRejectBtn.addEventListener('click', () => {
                const reason = document.getElementById('rejectionReasonInput').value.trim();
                if (!reason) {
                    alert('Please enter a rejection reason.');
                    return;
                }
                rejectApplication(loanId, reason);
            });
        }
        
        const disbBtn = document.getElementById('disburseLoanBtn');
        if (disbBtn) disbBtn.addEventListener('click', () => disburseLoan(loanId, disbBtn));
    }
});

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

async function loadOfficerQueue(isDashboard = false) {
    const loading = document.getElementById('queueLoading');
    const errorBox = document.getElementById('queueError');
    const emptyState = document.getElementById('queueEmptyState');
    const queueCard = document.getElementById('queueCard');
    const tbody = document.getElementById('queueTableBody');
    
    try {
        const res = await apiFetch('/api/loan-officer/applications');
        loading.style.display = 'none';
        
        if (res.status === 200) {
            const apps = await res.json();
            
            if (isDashboard) {
                const pending = apps.filter(a => a.status === 'SUBMITTED').length;
                const review = apps.filter(a => a.status === 'UNDER_REVIEW').length;
                
                const dashPending = document.getElementById('dashPendingCount');
                if (dashPending) dashPending.textContent = pending;
                
                const dashReview = document.getElementById('dashReviewCount');
                if (dashReview) dashReview.textContent = review;
            }
            
            if (apps.length === 0) {
                emptyState.style.display = 'block';
            } else {
                tbody.innerHTML = '';
                
                const displayApps = isDashboard ? apps.slice(0, 5) : apps;
                
                displayApps.forEach(app => {
                    const row = document.createElement('tr');
                    
                    const amountDisplay = '₹' + app.requestedAmount.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
                    
                    let actionText = 'Review';
                    if (app.status === 'UNDER_REVIEW') actionText = 'Continue';
                    
                    if (isDashboard) {
                        row.innerHTML = `
                            <td>${app.applicationNumber || 'N/A'}</td>
                            <td>${app.loanType}</td>
                            <td>${amountDisplay}</td>
                            <td><span class="badge ${getStatusBadgeClass(app.status)}">${app.status}</span></td>
                            <td><a href="/officer/applications/${app.id}" class="btn btn-primary btn-sm">${actionText}</a></td>
                        `;
                    } else {
                        let dtiDisplay = 'N/A';
                        if (app.debtToIncomeRatio != null) dtiDisplay = (app.debtToIncomeRatio * 100).toFixed(2) + '%';
                        
                        let eligDisplay = 'N/A';
                        if (app.eligibilityDecision) {
                            const eligClass = app.eligibilityDecision === 'ELIGIBLE' ? 'text-success' : 'text-danger';
                            eligDisplay = `<span class="${eligClass}">${app.eligibilityDecision}</span>`;
                        }
                        
                        row.innerHTML = `
                            <td>${app.applicationNumber || 'N/A'}</td>
                            <td>${app.loanType}</td>
                            <td>${amountDisplay}</td>
                            <td>${dtiDisplay}</td>
                            <td>${eligDisplay}</td>
                            <td><span class="badge ${getStatusBadgeClass(app.status)}">${app.status}</span></td>
                            <td><a href="/officer/applications/${app.id}" class="btn btn-primary btn-sm">${actionText}</a></td>
                        `;
                    }
                    tbody.appendChild(row);
                });
                queueCard.style.display = 'block';
            }
        } else {
            const err = await res.json().catch(() => ({}));
            errorBox.textContent = "Failed to load applications queue: " + (err.message || 'Unknown error');
            errorBox.style.display = 'block';
        }
    } catch (error) {
        loading.style.display = 'none';
        errorBox.textContent = "An error occurred while loading queue.";
        errorBox.style.display = 'block';
    }
}

async function loadOfficerApplication(id) {
    const loading = document.getElementById('detailsLoading');
    const errorBox = document.getElementById('detailsError');
    const content = document.getElementById('detailsContent');
    
    try {
        const res = await apiFetch(`/api/loan-officer/applications/${id}`);
        loading.style.display = 'none';
        
        if (res.status === 200) {
            const app = await res.json();
            
            document.getElementById('detailAppNumber').textContent = `Application Review - ${app.applicationNumber || 'N/A'}`;
            
            const badge = document.getElementById('detailStatusBadge');
            badge.textContent = app.status;
            badge.className = `badge ${getStatusBadgeClass(app.status)}`;
            
            document.getElementById('detailAmount').textContent = '₹' + app.requestedAmount.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
            document.getElementById('detailTenure').textContent = app.tenureMonths;
            document.getElementById('detailType').textContent = app.loanType;
            document.getElementById('detailPurpose').textContent = app.purpose;
            document.getElementById('detailSubmittedAt').textContent = app.submittedAt ? new Date(app.submittedAt).toLocaleString() : 'N/A';
            
            // Eligibility
            if (app.eligibilityDecision) {
                document.getElementById('detailDecision').textContent = app.eligibilityDecision;
                document.getElementById('detailDecision').className = app.eligibilityDecision === 'ELIGIBLE' ? 'text-success' : 'text-danger';
                document.getElementById('detailDti').textContent = app.debtToIncomeRatio != null ? (app.debtToIncomeRatio * 100).toFixed(2) + '%' : 'N/A';
                document.getElementById('detailLti').textContent = app.loanToIncomeRatio != null ? (app.loanToIncomeRatio * 100).toFixed(2) + '%' : 'N/A';
                document.getElementById('detailEmi').textContent = app.estimatedEmi != null ? '₹' + app.estimatedEmi.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) : 'N/A';
            } else {
                document.getElementById('detailDecision').textContent = 'Not Evaluated';
                document.getElementById('detailDti').textContent = 'N/A';
                document.getElementById('detailLti').textContent = 'N/A';
                document.getElementById('detailEmi').textContent = 'N/A';
            }
            
            // Review Info
            if (app.reviewedByEmail) {
                document.getElementById('reviewInfoCard').style.display = 'block';
                document.getElementById('detailReviewedBy').textContent = app.reviewedByEmail;
                document.getElementById('detailReviewedAt').textContent = app.reviewedAt ? new Date(app.reviewedAt).toLocaleString() : 'N/A';
                
                if (app.rejectionReason) {
                    document.getElementById('detailRejectionReasonContainer').style.display = 'block';
                    document.getElementById('detailRejectionReason').textContent = app.rejectionReason;
                }
            }
            
            // Actions
            const actionCard = document.getElementById('actionCard');
            const startBtn = document.getElementById('startReviewBtn');
            const approveBtn = document.getElementById('approveBtn');
            const rejectBtn = document.getElementById('rejectBtn');
            const rejectFormContainer = document.getElementById('rejectFormContainer');
            
            actionCard.style.display = 'none';
            startBtn.style.display = 'none';
            approveBtn.style.display = 'none';
            rejectBtn.style.display = 'none';
            rejectFormContainer.style.display = 'none';
            
            if (app.status === 'SUBMITTED') {
                actionCard.style.display = 'block';
                startBtn.style.display = 'inline-block';
            } else if (app.status === 'UNDER_REVIEW') {
                actionCard.style.display = 'block';
                approveBtn.style.display = 'inline-block';
                rejectBtn.style.display = 'inline-block';
            }
            
            if (app.status === 'APPROVED' || app.status === 'CLOSED') {
                document.getElementById('detailDisbursementCard').style.display = 'block';
                loadDisbursement(id, app.status);
                
                document.getElementById('detailRepaymentCard').style.display = 'block';
                loadRepayments(id, app.status);
                
                document.getElementById('detailOverdueCard').style.display = 'block';
                loadOverdue(id);
                
                document.getElementById('detailSettlementCard').style.display = 'block';
                loadSettlementQuote(id, app.status);
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

async function startReview(id) {
    const errorBox = document.getElementById('detailsError');
    const btn = document.getElementById('startReviewBtn');
    
    errorBox.style.display = 'none';
    btn.disabled = true;
    btn.textContent = 'Starting...';
    
    try {
        const res = await apiFetch(`/api/loan-officer/applications/${id}/review`, { method: 'POST' });
        
        if (res.status === 200) {
            window.location.reload();
        } else {
            const err = await res.json().catch(() => ({}));
            errorBox.textContent = err.message || "Failed to start review.";
            errorBox.style.display = 'block';
            btn.disabled = false;
            btn.textContent = 'Start Review';
        }
    } catch (error) {
        errorBox.textContent = "An error occurred.";
        errorBox.style.display = 'block';
        btn.disabled = false;
        btn.textContent = 'Start Review';
    }
}

async function approveApplication(id) {
    if (!confirm("Are you sure you want to approve this application?")) return;
    
    const errorBox = document.getElementById('detailsError');
    const btn = document.getElementById('approveBtn');
    
    errorBox.style.display = 'none';
    btn.disabled = true;
    btn.textContent = 'Approving...';
    
    try {
        const res = await apiFetch(`/api/loan-officer/applications/${id}/approve`, { method: 'POST' });
        
        if (res.status === 200) {
            window.location.reload();
        } else {
            const err = await res.json().catch(() => ({}));
            errorBox.textContent = err.message || "Failed to approve application.";
            errorBox.style.display = 'block';
            btn.disabled = false;
            btn.textContent = 'Approve Application';
        }
    } catch (error) {
        errorBox.textContent = "An error occurred.";
        errorBox.style.display = 'block';
        btn.disabled = false;
        btn.textContent = 'Approve Application';
    }
}

async function rejectApplication(id, reason) {
    const errorBox = document.getElementById('detailsError');
    const btn = document.getElementById('confirmRejectBtn');
    
    errorBox.style.display = 'none';
    btn.disabled = true;
    btn.textContent = 'Rejecting...';
    
    try {
        const res = await apiFetch(`/api/loan-officer/applications/${id}/reject`, {
            method: 'POST',
            body: JSON.stringify({ reason: reason })
        });
        
        if (res.status === 200) {
            window.location.reload();
        } else {
            const err = await res.json().catch(() => ({}));
            errorBox.textContent = err.message || "Failed to reject application.";
            errorBox.style.display = 'block';
            btn.disabled = false;
            btn.textContent = 'Confirm Rejection';
        }
    } catch (error) {
        errorBox.textContent = "An error occurred.";
        errorBox.style.display = 'block';
        btn.disabled = false;
        btn.textContent = 'Confirm Rejection';
    }
}

// ==========================================
// DISBURSEMENT LOGIC (Phase F8)
// ==========================================

async function loadDisbursement(applicationId, status) {
    const loading = document.getElementById('disbursementLoading');
    const errorBox = document.getElementById('disbursementError');
    const emptyMsg = document.getElementById('disbursementEmpty');
    const content = document.getElementById('disbursementContent');
    const btn = document.getElementById('disburseLoanBtn');
    
    loading.style.display = 'block';
    errorBox.style.display = 'none';
    emptyMsg.style.display = 'none';
    content.style.display = 'none';
    btn.style.display = 'none';
    
    try {
        const res = await apiFetch(`/api/loans/${applicationId}/disbursement`);
        loading.style.display = 'none';
        
        if (res.status === 200) {
            const data = await res.json();
            renderDisbursementData(data);
        } else if (res.status === 404) {
            emptyMsg.style.display = 'block';
            if (status === 'APPROVED') {
                btn.style.display = 'inline-block';
            }
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

async function disburseLoan(applicationId, btn) {
    const errorBox = document.getElementById('disbursementError');
    const emptyMsg = document.getElementById('disbursementEmpty');
    
    errorBox.style.display = 'none';
    emptyMsg.style.display = 'none';
    
    btn.disabled = true;
    btn.textContent = 'Disbursing...';
    
    try {
        const res = await apiFetch(`/api/loans/${applicationId}/disbursement`, {
            method: 'POST'
        });
        
        if (res.status === 201 || res.status === 200) {
            const data = await res.json();
            btn.style.display = 'none';
            renderDisbursementData(data);
        } else {
            const err = await res.json().catch(() => ({}));
            errorBox.textContent = err.message || 'Disbursement failed.';
            errorBox.style.display = 'block';
            btn.disabled = false;
            btn.textContent = 'Disburse Loan';
        }
    } catch (error) {
        errorBox.textContent = "An error occurred during disbursement.";
        errorBox.style.display = 'block';
        btn.disabled = false;
        btn.textContent = 'Disburse Loan';
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

async function loadRepayments(applicationId, appStatus) {
    const loading = document.getElementById('repaymentLoading');
    const errorBox = document.getElementById('repaymentError');
    const content = document.getElementById('repaymentContent');
    const emptyHistory = document.getElementById('repaymentHistoryEmpty');
    const historyTable = document.getElementById('repaymentHistoryTable');
    const btn = document.getElementById('recordRepaymentBtn');
    
    loading.style.display = 'block';
    errorBox.style.display = 'none';
    content.style.display = 'none';
    btn.style.display = 'none';
    
    try {
        const [summaryRes, historyRes] = await Promise.all([
            apiFetch(`/api/loans/${applicationId}/repayment-summary`),
            apiFetch(`/api/loans/${applicationId}/repayments`)
        ]);
        
        loading.style.display = 'none';
        
        if (summaryRes.status === 200 && historyRes.status === 200) {
            const summaryData = await summaryRes.json();
            const historyData = await historyRes.json();
            
            // Only show payment button if outstanding balance exists and loan is disbursed/closed
            if (summaryData.totalOutstanding > 0 && appStatus === 'APPROVED') {
                btn.style.display = 'inline-block';
                
                // Bind submit button
                const submitBtn = document.getElementById('submitRepaymentBtn');
                const oldBtn = submitBtn.cloneNode(true);
                submitBtn.parentNode.replaceChild(oldBtn, submitBtn); // remove old listeners
                
                document.getElementById('submitRepaymentBtn').addEventListener('click', () => {
                    submitRepayment(applicationId, appStatus);
                });
            }
            
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
                        <td>${new Date(rep.paidAt).toLocaleString()}</td>
                        <td>${rep.transactionReference}</td>
                        <td><strong>₹${rep.amount.toLocaleString('en-IN', { minimumFractionDigits: 2 })}</strong></td>
                        <td><span class="badge badge-success">${rep.status}</span></td>
                        <td>${allocationsHtml}</td>
                    `;
                    tbody.appendChild(row);
                });
            }
            
            content.style.display = 'block';
            
        } else if (summaryRes.status === 404 || historyRes.status === 404) {
            errorBox.textContent = "Repayment is available after disbursement.";
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

async function submitRepayment(applicationId, appStatus) {
    const errorBox = document.getElementById('repaymentFormError');
    const input = document.getElementById('repaymentAmount');
    const btn = document.getElementById('submitRepaymentBtn');
    
    errorBox.style.display = 'none';
    
    const amount = parseFloat(input.value);
    if (!amount || amount <= 0) {
        errorBox.textContent = "Please enter a valid positive amount.";
        errorBox.style.display = 'block';
        return;
    }
    
    btn.disabled = true;
    btn.textContent = 'Processing...';
    
    try {
        const res = await apiFetch(`/api/loans/${applicationId}/repayments`, {
            method: 'POST',
            body: JSON.stringify({ amount: amount })
        });
        
        if (res.status === 201 || res.status === 200) {
            $('#repaymentModal').modal('hide');
            input.value = '';
            btn.disabled = false;
            btn.textContent = 'Submit Payment';
            loadRepayments(applicationId, appStatus);
        } else {
            const err = await res.json().catch(() => ({}));
            errorBox.textContent = err.message || 'Repayment failed.';
            errorBox.style.display = 'block';
            btn.disabled = false;
            btn.textContent = 'Submit Payment';
        }
    } catch (error) {
        errorBox.textContent = "An error occurred during repayment.";
        errorBox.style.display = 'block';
        btn.disabled = false;
        btn.textContent = 'Submit Payment';
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
    
    if (!loading) return; // In case we are not on application-details.html
    
    loading.style.display = 'block';
    errorBox.style.display = 'none';
    emptyBox.style.display = 'none';
    content.style.display = 'none';
    
    try {
        const res = await apiFetch(`/api/loan-officer/applications/${applicationId}/overdue`);
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
            emptyBox.style.display = 'block';
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

async function loadGlobalOverdue() {
    const loading = document.getElementById('globalOverdueLoading');
    const emptyBox = document.getElementById('globalOverdueEmpty');
    const content = document.getElementById('globalOverdueContent');
    const tbody = document.getElementById('globalOverdueBody');
    const alertBox = document.getElementById('globalOverdueProcessAlert');
    
    if (!loading) return;
    
    loading.style.display = 'block';
    emptyBox.style.display = 'none';
    content.style.display = 'none';
    if(alertBox) alertBox.style.display = 'none';
    
    try {
        const appsRes = await apiFetch('/api/loan-officer/applications');
        if (appsRes.status !== 200) {
            loading.textContent = "Failed to load applications.";
            return;
        }
        const apps = await appsRes.json();
        
        const activeApps = apps.filter(a => a.status === 'APPROVED');
        let allOverdues = [];
        
        // Fetch overdues for each active application
        for (let app of activeApps) {
            const overdueRes = await apiFetch(`/api/loan-officer/applications/${app.id}/overdue`);
            if (overdueRes.status === 200) {
                const overdues = await overdueRes.json();
                overdues.forEach(od => {
                    od.appId = app.id;
                    allOverdues.push(od);
                });
            }
        }
        
        loading.style.display = 'none';
        
        if (allOverdues.length === 0) {
            emptyBox.style.display = 'block';
        } else {
            tbody.innerHTML = '';
            allOverdues.forEach(od => {
                const row = document.createElement('tr');
                row.innerHTML = `
                    <td><a href="/officer/applications/${od.appId}">APP-${od.appId}</a></td>
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
        
    } catch (error) {
        loading.textContent = "An error occurred while loading global overdues.";
    }
}

async function processGlobalOverdue() {
    const btn = document.getElementById('processOverdueBtn');
    const alertBox = document.getElementById('globalOverdueProcessAlert');
    
    btn.disabled = true;
    btn.textContent = 'Processing...';
    alertBox.style.display = 'none';
    
    try {
        const res = await apiFetch('/api/loan-officer/overdue/process', {
            method: 'POST'
        });
        
        if (res.status === 200) {
            const data = await res.json();
            alertBox.className = 'alert alert-success mt-3';
            alertBox.innerHTML = `<strong>Success:</strong> Processed ${data.processedInstallments} installments. New penalties created: ${data.newPenaltiesCreated}. Total penalty value: ₹${data.totalPenaltyCreated.toLocaleString('en-IN', { minimumFractionDigits: 2 })}.`;
            alertBox.style.display = 'block';
            
            // Reload table
            await loadGlobalOverdue();
        } else {
            const err = await res.json().catch(() => ({}));
            alertBox.className = 'alert alert-danger mt-3';
            alertBox.innerHTML = `<strong>Error:</strong> ${err.message || 'Processing failed'}`;
            alertBox.style.display = 'block';
        }
    } catch (error) {
        alertBox.className = 'alert alert-danger mt-3';
        alertBox.innerHTML = `<strong>Error:</strong> An unexpected error occurred.`;
        alertBox.style.display = 'block';
    } finally {
        btn.disabled = false;
        btn.textContent = 'Run Overdue Check';
    }
}

// ==========================================
// SETTLEMENT / PREPAYMENT LOGIC (Phase F11)
// ==========================================

async function loadSettlementQuote(applicationId, loanStatus) {
    const loading = document.getElementById('settlementLoading');
    const errorBox = document.getElementById('settlementError');
    const emptyBox = document.getElementById('settlementEmpty');
    const content = document.getElementById('settlementContent');
    const formSection = document.getElementById('recordPrepaymentSection');
    
    if (!loading) return; // if not on application-details.html
    
    loading.style.display = 'block';
    errorBox.style.display = 'none';
    emptyBox.style.display = 'none';
    content.style.display = 'none';
    formSection.style.display = 'none';
    
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
            
            if (loanStatus === 'APPROVED') {
                formSection.style.display = 'block';
                document.getElementById('prepaymentApplicationId').value = applicationId;
                document.getElementById('prepaymentAmount').value = ''; // clean input
            }
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

async function submitPrepayment() {
    const appId = document.getElementById('prepaymentApplicationId').value;
    const type = document.getElementById('prepaymentType').value;
    const amountStr = document.getElementById('prepaymentAmount').value;
    
    const amount = parseFloat(amountStr);
    
    const btn = document.getElementById('submitPrepaymentBtn');
    const alertBox = document.getElementById('prepaymentAlert');
    
    btn.disabled = true;
    btn.textContent = 'Processing...';
    alertBox.style.display = 'none';
    
    const payload = {
        amount: amount,
        prepaymentType: type
    };
    
    try {
        const res = await apiFetch(`/api/loan-officer/applications/${appId}/prepayments`, {
            method: 'POST',
            body: JSON.stringify(payload)
        });
        
        if (res.status === 201) {
            const data = await res.json();
            alertBox.className = 'alert alert-success';
            alertBox.innerHTML = `<strong>Success:</strong> Prepayment of ₹${data.amount.toLocaleString('en-IN', { minimumFractionDigits: 2 })} recorded. Ref: ${data.transactionReference}`;
            alertBox.style.display = 'block';
            
            // Reload EVERYTHING after a timeout so user sees success message
            setTimeout(() => {
                viewApplication(appId);
            }, 2000);
        } else if (res.status === 409) {
            alertBox.className = 'alert alert-warning';
            alertBox.innerHTML = `<strong>Conflict:</strong> Loan state has changed. Refreshing...`;
            alertBox.style.display = 'block';
            setTimeout(() => {
                viewApplication(appId);
            }, 2000);
        } else {
            const err = await res.json().catch(() => ({}));
            alertBox.className = 'alert alert-danger';
            alertBox.innerHTML = `<strong>Error:</strong> ${err.message || 'Validation failed'}`;
            alertBox.style.display = 'block';
            btn.disabled = false;
            btn.textContent = 'Submit Prepayment';
        }
    } catch (error) {
        alertBox.className = 'alert alert-danger';
        alertBox.innerHTML = `<strong>Error:</strong> An unexpected error occurred.`;
        alertBox.style.display = 'block';
        btn.disabled = false;
        btn.textContent = 'Submit Prepayment';
    }
}
