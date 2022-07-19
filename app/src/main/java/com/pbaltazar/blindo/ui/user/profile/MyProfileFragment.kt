package com.pbaltazar.blindo.ui.user.profile

import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import com.pbaltazar.blindo.R
import com.pbaltazar.blindo.databinding.FragmentMyProfileBinding
import com.pbaltazar.blindo.entities.User
import com.pbaltazar.blindo.utils.authentication.ui.AuthenticableFragment
import com.pbaltazar.blindo.utils.authentication.ui.AuthenticationViewModel

class MyProfileFragment : AuthenticableFragment<FragmentMyProfileBinding>() {

    private lateinit var userCrown: ImageView
    private lateinit var userName: EditText
    private lateinit var emailLabel: TextView
    private lateinit var userEmail: EditText
    private lateinit var isVerified: TextView
    private lateinit var saveUser: Button

    private var isEditing: Boolean = false
    set(value) {
        field = value
        saveUser.apply {
            if (value) {
                text = getString(R.string.profile__save)
                visibility = View.VISIBLE
            } else {
                visibility = View.GONE
            }
        }
        userName.apply {
            isEnabled = value
            hint = if (value)
                getString(R.string.profile__name_hint)
            else
                ""
        }
    }

    override val isSearchable: Boolean
        get() = false

    override fun getMenuResId(): Int = R.menu.user_profile

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        subscribeUser()
        subscribeUserUpdates()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentMyProfileBinding.inflate(inflater, container, false)
        userCrown = binding!!.userCrown
        userName = binding!!.userName
        emailLabel = binding!!.emailLabel
        userEmail = binding!!.userEmail
        isVerified = binding!!.isVerified
        saveUser = binding!!.saveUser
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        authenticateUser()
        setupUi()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menuEditProfile -> {
                item.apply {
                    isEditing = isEditing.not()
                    if (isEditing) {
                        setIcon(R.drawable.ic_done_black_24dp)
                        title = getString(R.string.profile__done)
                    } else {
                        setIcon(R.drawable.ic_input_black_24dp)
                        title = getString(R.string.profile__edit)
                    }
                }
                return true
            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onSubscribeUser(user: User?) {
        user?.also { currentUser ->
            if (isEditing.not()) {
                userCrown.apply {
                    visibility = if (currentUser.isPremium)
                        View.VISIBLE
                    else
                        View.GONE
                }
                userName.text = Editable.Factory.getInstance().newEditable(currentUser.name)
                userEmail.text = Editable.Factory.getInstance().newEditable(currentUser.email)
                isVerified.apply {
                    text = if (currentUser.isVerified)
                        getString(R.string.profile__verified)
                    else
                        getString(R.string.profile__not_verified)
                }
            }
        }
    }

    override fun onSubscribeUserUpdates(userUpdate: AuthenticationViewModel.UserUpdate) {
        when (userUpdate) {
            is AuthenticationViewModel.UserUpdate.Success -> processUpdateResult(getString(R.string.profile__update_success))
            is AuthenticationViewModel.UserUpdate.BadRequest -> processUpdateResult(getString(R.string.profile__updating_error, userUpdate.errors.joinToString(", ")))
            is AuthenticationViewModel.UserUpdate.NetworkEror -> processUpdateResult(getString(R.string.profile__updating_error, userUpdate.throwable.localizedMessage))
            else -> processUpdateResult(getString(R.string.profile__unknown_error_updating))
        }
    }

    private fun showUpdateStatus(message: String) =         Snackbar.make(
        saveUser,
        message,
        Snackbar.LENGTH_LONG
    ).show()

    private fun processUpdateResult(statusMessage: String) {
        userName.isEnabled = true
        saveUser.apply {
            text = getString(R.string.profile__save)
            isEnabled = true
        }
        showUpdateStatus(statusMessage)
    }

    private fun setupUi() {
        if (Build.VERSION.SDK_INT >= 22) {
            userCrown.accessibilityTraversalBefore = R.id.userName
            emailLabel.accessibilityTraversalAfter = R.id.userName
            userEmail.accessibilityTraversalAfter = R.id.emailLabel
        }
        userName.setOnEditorActionListener { v, _, _ ->
            if (v.text.length < 6) {
                saveUser.isEnabled = false
            } else {
                saveUser.isEnabled = true
            }
            return@setOnEditorActionListener true
        }
        saveUser.setOnClickListener {
            saveUser.apply {
                text = getString(R.string.viewstate__loading_title)
                isEnabled = false
            }
            userName.apply {
                isEnabled = false
                text?.toString()?.also { name ->
                    getUser()?.also { user ->
                        updateUser(
                            user.copy(
                                name = name
                            )
                        )
                    } ?: processUpdateResult(getString(R.string.profile__not_signed_in))
                } ?: processUpdateResult(
                    getString(R.string.profile__name_empty)
                )
            }
        }
    }
}
